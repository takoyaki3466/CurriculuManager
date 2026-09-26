package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GraduationRequirementsTable {
    public static void createGraduationRequirementsTable(Connection connection) throws SQLException {
        GraduationRequirementCategoriesTable.createGraduationRequirementCategoriesTable(connection);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS graduation_requirements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        curriculum_id INTEGER NOT NULL,
                        category_id INTEGER,
                        name TEXT NOT NULL,
                        required_credits REAL NOT NULL,
                        FOREIGN KEY (curriculum_id)
                            REFERENCES curricula(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY (category_id)
                            REFERENCES course_categories(id)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }
}
