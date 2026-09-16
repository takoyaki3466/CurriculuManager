package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 学科テーブルを管理するクラス。
 *
 * <p>
 * 学科は必ず1つの学部に所属します。
 * </p>
 */
public final class MajorsTable {

    private MajorsTable() {
    }

    /**
     * 学科テーブルを作成します。
     *
     * <p>
     * すでにテーブルが存在する場合は何もしません。
     * </p>
     *
     * @param connection データベース接続
     * @throws SQLException テーブル作成に失敗した場合
     */
    public static void createMajorsTable(Connection connection) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS majors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    department_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    FOREIGN KEY (department_id)
                        REFERENCES departments(id)
                        ON DELETE CASCADE,
                    UNIQUE(department_id, name)
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
