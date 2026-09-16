package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CourseTable {
    /**
     * 科目カテゴリーテーブル
     * <p>
     * parent_idを持たせることで、
     * <p>
     * 一般教養
     * ├─ グループ1
     * ├─ グループ2
     * └─ グループ3
     * <p>
     * のような階層構造を作れます。
     */
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

    /**
     * 科目テーブル
     * <p>
     * Courseは「授業そのもの」です。
     * <p>
     * 必修・選択はここには持たせません。
     */
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
