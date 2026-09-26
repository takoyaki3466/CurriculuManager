package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DepartmentsTable {
    public static void createDepartmentsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS departments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE
                    )
                    """);
        }
    }
}
