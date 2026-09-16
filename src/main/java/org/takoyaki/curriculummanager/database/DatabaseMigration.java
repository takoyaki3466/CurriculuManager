package org.takoyaki.curriculummanager.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * データベースのバージョンアップを管理するクラス。
 *
 * <p>
 * データベースの構造を変更した場合、
 * 既存のデータベースを新しい構造へ移行します。
 * </p>
 */
public final class DatabaseMigration {

    /**
     * 現在のデータベース構造のバージョン。
     */
    private static final int CURRENT_VERSION = 4;

    private DatabaseMigration() {
    }

    /**
     * データベースのマイグレーションを実行します。
     */
    public static void migrate() {

        try (Connection connection = DatabaseManager.getConnection()) {

            int version = getDatabaseVersion(connection);

            /*
             * 新規データベースの場合、
             * DatabaseInitによって最新版のテーブルが
             * すでに作成されています。
             *
             * この状態でversion=0のまま
             * 過去のマイグレーションを実行すると、
             *
             * ADD COLUMN major_id
             *
             * などが重複して実行されてしまいます。
             *
             * そのため、最新版のスキーマがすでに存在する
             * version=0のデータベースは、
             * そのまま最新版(version=4)として扱います。
             */
            if (version == 0 && isCurrentSchema(connection)) {

                setDatabaseVersion(connection, CURRENT_VERSION);

                return;
            }

            /*
             * バージョン0 → 1
             *
             * enrollments に curriculum_id を追加します。
             */
            if (version < 1) {

                migrateV0ToV1(connection);

                version = 1;

                setDatabaseVersion(connection, version);
            }

            /*
             * バージョン1 → 2
             *
             * curricula に major_id を追加します。
             */
            if (version < 2) {

                migrateV1ToV2(connection);

                version = 2;

                setDatabaseVersion(connection, version);
            }

            /*
             * バージョン2 → 3
             *
             * curricula を再作成し、
             * major_id を必須にします。
             */
            if (version < 3) {

                migrateV2ToV3(connection);

                version = 3;

                setDatabaseVersion(connection, version);
            }

            /*
             * バージョン3 → 4
             *
             * curricula の
             *
             * UNIQUE(major_id, start_year)
             *
             * を削除します。
             *
             * これにより、同じ学科・同じ年度に
             * 複数のカリキュラムを登録できます。
             */
            if (version < 4) {

                migrateV3ToV4(connection);

                version = 4;

                setDatabaseVersion(connection, version);
            }

        } catch (SQLException e) {

            throw new RuntimeException("データベースのマイグレーションに失敗しました。", e);
        }
    }

    /**
     * 現在のデータベースが最新版の構造になっているか確認します。
     *
     * <p>
     * DatabaseInitはマイグレーションより先に
     * 現在のテーブル構造を作成するため、
     * 新規データベースではuser_versionが0でも
     * 最新の構造になっています。
     * </p>
     *
     * <p>
     * 以下の条件を確認します。
     * </p>
     *
     * <ul>
     *     <li>curriculaにmajor_idが存在する</li>
     *     <li>enrollmentsにcurriculum_idが存在する</li>
     * </ul>
     *
     * @param connection データベース接続
     * @return 最新構造ならtrue
     */
    private static boolean isCurrentSchema(Connection connection) throws SQLException {

        boolean curriculaHasMajorId = hasColumn(connection, "curricula", "major_id");

        boolean enrollmentsHasCurriculumId = hasColumn(connection, "enrollments", "curriculum_id");

        return curriculaHasMajorId && enrollmentsHasCurriculumId;
    }

    /**
     * 指定されたテーブルに指定された列が存在するか確認します。
     *
     * @param connection データベース接続
     * @param tableName  テーブル名
     * @param columnName 列名
     * @return 列が存在すればtrue
     */
    private static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();

             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {

                String currentColumn = resultSet.getString("name");

                if (columnName.equalsIgnoreCase(currentColumn)) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 現在のデータベースバージョンを取得します。
     */
    private static int getDatabaseVersion(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement();

             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {

            if (resultSet.next()) {

                return resultSet.getInt(1);
            }
        }

        return 0;
    }

    /**
     * データベースバージョンを設定します。
     */
    private static void setDatabaseVersion(Connection connection, int version) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA user_version = " + version);
        }
    }

    /**
     * バージョン0から1へ移行します。
     *
     * <p>
     * enrollments テーブルに
     * curriculum_id を追加します。
     * </p>
     */
    private static void migrateV0ToV1(Connection connection) throws SQLException {

        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = OFF");

            statement.execute("""
                    CREATE TABLE enrollments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                    
                        course_id INTEGER NOT NULL,
                    
                        curriculum_id INTEGER,
                    
                        year INTEGER NOT NULL,
                    
                        semester TEXT NOT NULL,
                    
                        grade_id INTEGER,
                    
                        FOREIGN KEY (course_id)
                            REFERENCES courses(id)
                            ON DELETE CASCADE,
                    
                        FOREIGN KEY (curriculum_id)
                            REFERENCES curricula(id)
                            ON DELETE CASCADE,
                    
                        FOREIGN KEY (grade_id)
                            REFERENCES grade_definitions(id)
                            ON DELETE SET NULL,
                    
                        UNIQUE(course_id, year, semester)
                    )
                    """);

            statement.execute("""
                    INSERT INTO enrollments_new (
                        id,
                        course_id,
                        year,
                        semester,
                        grade_id
                    )
                    SELECT
                        id,
                        course_id,
                        year,
                        semester,
                        grade_id
                    FROM enrollments
                    """);

            statement.execute("DROP TABLE enrollments");

            statement.execute("ALTER TABLE enrollments_new " + "RENAME TO enrollments");

            statement.execute("PRAGMA foreign_keys = ON");

            statement.execute("PRAGMA foreign_key_check");

            connection.commit();

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(true);
        }
    }

    /**
     * バージョン1から2へ移行します。
     *
     * <p>
     * curricula テーブルに major_id を追加します。
     * </p>
     *
     * <p>
     * すでに major_id が存在する場合は、
     * 何もせず次のバージョンへ進みます。
     * </p>
     */
    private static void migrateV1ToV2(Connection connection) throws SQLException {

        /*
         * すでにmajor_idが存在するか確認します。
         *
         * 過去のマイグレーション途中で
         * major_idだけ追加されている場合でも、
         * ADD COLUMNを二重実行しないようにします。
         */
        if (hasColumn(connection, "curricula", "major_id")) {

            return;
        }

        try (Statement statement = connection.createStatement()) {

            statement.execute("ALTER TABLE curricula " + "ADD COLUMN major_id INTEGER");
        }
    }


    /**
     * バージョン2から3へ移行します。
     *
     * <p>
     * curricula テーブルを再作成し、
     * major_id を NOT NULL に変更します。
     * </p>
     *
     * <p>
     * 旧データについて major_id が設定されている
     * カリキュラムのみを移行します。
     * </p>
     */
    private static void migrateV2ToV3(Connection connection) throws SQLException {

        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = OFF");

            statement.execute("""
                    CREATE TABLE curricula_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                    
                        major_id INTEGER NOT NULL,
                    
                        name TEXT NOT NULL,
                    
                        start_year INTEGER NOT NULL,
                    
                        FOREIGN KEY (major_id)
                            REFERENCES majors(id)
                            ON DELETE CASCADE,
                    
                        UNIQUE(major_id, start_year)
                    )
                    """);

            statement.execute("""
                    INSERT INTO curricula_new (
                        id,
                        major_id,
                        name,
                        start_year
                    )
                    SELECT
                        id,
                        major_id,
                        name,
                        start_year
                    FROM curricula
                    WHERE major_id IS NOT NULL
                    """);

            statement.execute("DROP TABLE curricula");

            statement.execute("ALTER TABLE curricula_new " + "RENAME TO curricula");

            statement.execute("PRAGMA foreign_keys = ON");

            statement.execute("PRAGMA foreign_key_check");

            connection.commit();

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(true);
        }
    }

    /**
     * バージョン3から4へ移行します。
     *
     * <p>
     * curricula テーブルから
     * UNIQUE(major_id, start_year)
     * を削除します。
     * </p>
     *
     * <p>
     * これにより、同じ学科・同じ年度に
     * 複数のカリキュラムを登録できるようになります。
     * </p>
     */
    private static void migrateV3ToV4(Connection connection) throws SQLException {

        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = OFF");

            statement.execute("""
                    CREATE TABLE curricula_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                    
                        major_id INTEGER NOT NULL,
                    
                        name TEXT NOT NULL,
                    
                        start_year INTEGER NOT NULL,
                    
                        FOREIGN KEY (major_id)
                            REFERENCES majors(id)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    INSERT INTO curricula_new (
                        id,
                        major_id,
                        name,
                        start_year
                    )
                    SELECT
                        id,
                        major_id,
                        name,
                        start_year
                    FROM curricula
                    """);

            statement.execute("DROP TABLE curricula");

            statement.execute("ALTER TABLE curricula_new " + "RENAME TO curricula");

            statement.execute("PRAGMA foreign_keys = ON");

            statement.execute("PRAGMA foreign_key_check");

            connection.commit();

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(true);
        }
    }
}
