package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class MajorsTable {
    private MajorsTable() {
    }

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
