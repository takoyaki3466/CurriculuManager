package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class GraduationRequirementCategoriesTable {
    private GraduationRequirementCategoriesTable() {
    }

    public static void createGraduationRequirementCategoriesTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS graduation_requirement_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        requirement_id INTEGER NOT NULL,
                        category_id INTEGER NOT NULL,
                        FOREIGN KEY (requirement_id)
                            REFERENCES graduation_requirements(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY (category_id)
                            REFERENCES course_categories(id)
                            ON DELETE CASCADE,
                        UNIQUE(
                            requirement_id,
                            category_id
                        )
                    )
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                    idx_graduation_requirement_categories_requirement_id
                    ON graduation_requirement_categories(requirement_id)
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                    idx_graduation_requirement_categories_category_id
                    ON graduation_requirement_categories(category_id)
                    """);
        }
    }
}
