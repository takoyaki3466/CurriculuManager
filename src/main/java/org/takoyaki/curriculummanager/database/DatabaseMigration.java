package org.takoyaki.curriculummanager.database;

import org.takoyaki.curriculummanager.database.tables.GraduationRequirementCategoriesTable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseMigration {
    private static final int CURRENT_VERSION = 5;

    private DatabaseMigration() {
    }

    public static void migrate() {
        try (Connection connection = DatabaseManager.getConnection()) {
            int version = getDatabaseVersion(connection);

            if (version == 0 && isCurrentSchema(connection)) {
                setDatabaseVersion(connection, CURRENT_VERSION);
                return;
            }

            if (version < 1) {
                migrateV0ToV1(connection);
                version = 1;
                setDatabaseVersion(connection, version);
            }

            if (version < 2) {
                migrateV1ToV2(connection);
                version = 2;
                setDatabaseVersion(connection, version);
            }

            if (version < 3) {
                migrateV2ToV3(connection);
                version = 3;
                setDatabaseVersion(connection, version);
            }

            if (version < 4) {
                migrateV3ToV4(connection);
                version = 4;
                setDatabaseVersion(connection, version);
            }

            if (version < 5) {
                migrateV4ToV5(connection);
                version = 5;
                setDatabaseVersion(connection, version);
            }
        } catch (SQLException e) {
            throw new RuntimeException("データベースのマイグレーションに失敗しました。", e);
        }
    }

    private static boolean isCurrentSchema(Connection connection) throws SQLException {
        boolean curriculaHasMajorId = hasColumn(connection, "curricula", "major_id");
        boolean enrollmentsHasCurriculumId = hasColumn(connection, "enrollments", "curriculum_id");
        boolean hasRequirementCategoriesTable = hasTable(connection, "graduation_requirement_categories");
        return curriculaHasMajorId && enrollmentsHasCurriculumId && hasRequirementCategoriesTable;
    }

    private static boolean hasTable(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = '%s'
                """.formatted(tableName);

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        }
    }

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

    private static int getDatabaseVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }

        return 0;
    }

    private static void setDatabaseVersion(Connection connection, int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA user_version = " + version);
        }
    }

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

    private static void migrateV1ToV2(Connection connection) throws SQLException {
        if (hasColumn(connection, "curricula", "major_id")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE curricula " + "ADD COLUMN major_id INTEGER");
        }
    }

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

    private static void migrateV4ToV5(Connection connection) throws SQLException {
        connection.setAutoCommit(false);

        try {
            GraduationRequirementCategoriesTable.createGraduationRequirementCategoriesTable(connection);

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        INSERT OR IGNORE INTO
                        graduation_requirement_categories (
                            requirement_id,
                            category_id
                        )
                        SELECT
                            id,
                            category_id
                        FROM graduation_requirements
                        WHERE category_id IS NOT NULL
                        """);

                try (ResultSet resultSet = statement.executeQuery("PRAGMA foreign_key_check")) {
                    if (resultSet.next()) {
                        String table = resultSet.getString("table");
                        long rowId = resultSet.getLong("rowid");
                        String parent = resultSet.getString("parent");
                        throw new SQLException("外部キー整合性チェックに失敗しました。" + " table=" + table + ", rowid=" + rowId + ", parent=" + parent);
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
