package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GraduationRequirementsTable {
    /**
     * 卒業要件テーブル
     * <p>
     * category_idがNULLなら
     * カリキュラム全体の必要単位数。
     * <p>
     * category_idが指定されていれば
     * そのカテゴリーの必要単位数。
     */
    public static void createGraduationRequirementsTable(Connection connection) throws SQLException {

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
