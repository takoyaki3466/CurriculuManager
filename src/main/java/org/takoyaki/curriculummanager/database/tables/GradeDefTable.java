package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GradeDefTable {
    public static void createGradeDefinitionsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS grade_definitions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        symbol TEXT NOT NULL UNIQUE,
                        gpa_points REAL,
                        passed INTEGER NOT NULL DEFAULT 0,
                        included_in_gpa INTEGER NOT NULL DEFAULT 1
                    )
                    """);
        }
    }

    public static void insertDefaultGradeDefinitions(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT OR IGNORE INTO grade_definitions
                        (symbol, gpa_points, passed, included_in_gpa)
                    VALUES
                        ('S', 4.0, 1, 1),
                        ('A', 3.0, 1, 1),
                        ('B', 2.0, 1, 1),
                        ('C', 1.0, 1, 1),
                        ('D', 0.0, 0, 1),
                        ('N', NULL, 0, 0)
                    """);
        }
    }
}
