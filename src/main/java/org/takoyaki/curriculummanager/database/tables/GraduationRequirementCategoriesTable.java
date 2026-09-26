package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 卒業要件と科目カテゴリの関連テーブルを管理するクラスです。
 *
 * <p>
 * 1つの卒業要件に対して、
 * 複数の科目カテゴリを紐付けるために使用します。
 * </p>
 *
 * <p>
 * 例:
 * </p>
 *
 * <pre>
 * 基礎科学分野 14単位以上
 *
 *     数学系
 *     物理系
 *     化学系
 * </pre>
 *
 * <p>
 * 上記の場合、
 * 同じ requirement_id に対して
 * 3つの category_id が登録されます。
 * </p>
 *
 * <p>
 * 「全体」を対象とする卒業要件の場合は、
 * このテーブルには何も登録しません。
 * </p>
 */
public final class GraduationRequirementCategoriesTable {

    private GraduationRequirementCategoriesTable() {
    }

    /**
     * 卒業要件とカテゴリの中間テーブルを作成します。
     *
     * @param connection SQLite接続
     * @throws SQLException テーブル作成に失敗した場合
     */
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

            /*
             * requirement_id から関連カテゴリを検索することが多いため、
             * インデックスを作成しておきます。
             */
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                    idx_graduation_requirement_categories_requirement_id
                    ON graduation_requirement_categories(requirement_id)
                    """);

            /*
             * category_id 側から卒業要件を探す場合にも使用できます。
             */
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                    idx_graduation_requirement_categories_category_id
                    ON graduation_requirement_categories(category_id)
                    """);
        }
    }
}