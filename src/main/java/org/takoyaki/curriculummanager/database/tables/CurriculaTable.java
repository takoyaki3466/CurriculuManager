package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * カリキュラム関連のテーブルを管理するクラス。
 */
public class CurriculaTable {

    /**
     * カリキュラムテーブルを作成します。
     *
     * <p>
     * カリキュラムは学科に所属します。
     * </p>
     *
     * <p>
     * 同じ学科・同じ年度であっても、
     * 複数のカリキュラムを登録できるようにしています。
     * </p>
     */
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

    /**
     * カリキュラムと科目の関連テーブルを作成します。
     */
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
