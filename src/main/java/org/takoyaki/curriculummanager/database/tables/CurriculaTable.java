package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CurriculaTable {
    public static void createCurriculaTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS curricula (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        major_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        start_year INTEGER NOT NULL,
                        FOREIGN KEY (major_id)
                            REFERENCES majors(id)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    public static void createCurriculumCoursesTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS curriculum_courses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        curriculum_id INTEGER NOT NULL,
                        course_id INTEGER NOT NULL,
                        requirement_type TEXT NOT NULL,
                        FOREIGN KEY (curriculum_id)
                            REFERENCES curricula(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY (course_id)
                            REFERENCES courses(id)
                            ON DELETE CASCADE,
                        UNIQUE(curriculum_id, course_id)
                    )
                    """);
        }
    }
}
