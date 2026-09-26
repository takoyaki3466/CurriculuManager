package org.takoyaki.curriculummanager.database;

import org.takoyaki.curriculummanager.database.tables.CourseTable;
import org.takoyaki.curriculummanager.database.tables.CurriculaTable;
import org.takoyaki.curriculummanager.database.tables.DepartmentsTable;
import org.takoyaki.curriculummanager.database.tables.EnrollmentsTable;
import org.takoyaki.curriculummanager.database.tables.GradeDefTable;
import org.takoyaki.curriculummanager.database.tables.GraduationRequirementCategoriesTable;
import org.takoyaki.curriculummanager.database.tables.GraduationRequirementsTable;
import org.takoyaki.curriculummanager.database.tables.MajorsTable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * データベースの初期化を行うクラスです。
 *
 * <p>
 * アプリケーション起動時に必要なテーブルを作成し、
 * 初期データやマイグレーションを適用します。
 * </p>
 */
public final class DatabaseInit {

    private DatabaseInit() {
    }

    /**
     * データベースを初期化します。
     */
    public static void initialize() {

        try (Connection connection = DatabaseManager.getConnection()) {

            /*
             * 学部・学科
             */
            DepartmentsTable.createDepartmentsTable(connection);

            MajorsTable.createMajorsTable(connection);

            /*
             * カリキュラム
             */
            CurriculaTable.createCurriculaTable(connection);

            /*
             * 科目カテゴリ
             */
            CourseTable.createCourseCategoriesTable(connection);

            /*
             * 科目
             */
            CourseTable.createCoursesTable(connection);

            /*
             * カリキュラムと科目の関連
             */
            CurriculaTable.createCurriculumCoursesTable(connection);

            /*
             * 卒業要件
             */
            GraduationRequirementsTable.createGraduationRequirementsTable(connection);

            /*
             * 卒業要件とカテゴリの関連
             *
             * graduation_requirements と
             * course_categories の両方を参照するため、
             * それらのテーブルを作成した後に実行します。
             */
            GraduationRequirementCategoriesTable.createGraduationRequirementCategoriesTable(connection);

            /*
             * 成績定義
             */
            GradeDefTable.createGradeDefinitionsTable(connection);

            /*
             * 履修情報
             */
            EnrollmentsTable.createEnrollmentsTable(connection);

            /*
             * 標準の成績定義を登録
             */
            GradeDefTable.insertDefaultGradeDefinitions(connection);

        } catch (SQLException e) {

            throw new RuntimeException("データベースの初期化に失敗しました。", e);
        }

        /*
         * 成績関連の初期化
         */
        GradeDefInit.initialize();

        /*
         * 既存データベースのマイグレーション
         */
        DatabaseMigration.migrate();
    }
}