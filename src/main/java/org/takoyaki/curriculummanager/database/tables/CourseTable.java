package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CourseTable {
    public static void createCourseCategoriesTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS course_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        curriculum_id INTEGER NOT NULL,
                        parent_id INTEGER,
                        name TEXT NOT NULL,
                        display_order INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (curriculum_id)
                            REFERENCES curricula(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY (parent_id)
                            REFERENCES course_categories(id)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    public static void createCoursesTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS courses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_code TEXT UNIQUE,
                        name TEXT NOT NULL,
                        credits REAL NOT NULL,
                        category_id INTEGER,
                        description TEXT,
                        FOREIGN KEY (category_id)
                            REFERENCES course_categories(id)
                            ON DELETE SET NULL
                    )
                    """);
        }
    }
}
