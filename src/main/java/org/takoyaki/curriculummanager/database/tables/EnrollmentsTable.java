package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class EnrollmentsTable {
    /**
     * 履修記録テーブル
     */
    public static void createEnrollmentsTable(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS enrollments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
            
                        curriculum_id INTEGER,
            
                        course_id INTEGER NOT NULL,
            
                        year INTEGER NOT NULL,
            
                        semester TEXT NOT NULL,
            
                        grade_id INTEGER,
            
                        FOREIGN KEY (curriculum_id)
                            REFERENCES curricula(id)
                            ON DELETE CASCADE,
            
                        FOREIGN KEY (course_id)
                            REFERENCES courses(id)
                            ON DELETE CASCADE,
            
                        FOREIGN KEY (grade_id)
                            REFERENCES grade_definitions(id)
                            ON DELETE SET NULL,
            
                        UNIQUE(
                            curriculum_id,
                            course_id,
                            year,
                            semester
                        )
                    )
                    """
            );
        }
    }
}
