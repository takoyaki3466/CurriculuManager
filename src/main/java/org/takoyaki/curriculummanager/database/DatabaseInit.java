package org.takoyaki.curriculummanager.database;

import org.takoyaki.curriculummanager.database.tables.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInit {

    private DatabaseInit() {
    }

    /**
     * データベースを初期化します。
     * <p>
     * アプリ起動時に実行されます。
     * <p>
     * テーブルが既に存在する場合は、
     * CREATE TABLE IF NOT EXISTS によって
     * 既存のデータを破壊しません。
     */
    public static void initialize() {

        try (Connection connection = DatabaseManager.getConnection()) {

            /*
             * 外部キー制約を有効にする。
             */
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

            /*
             * 学部
             */
            DepartmentsTable.createDepartmentsTable(connection);

            /*
             * 学科
             */
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
             * 成績定義
             */
            GradeDefTable.createGradeDefinitionsTable(connection);

            /*
             * 履修情報
             */
            EnrollmentsTable.createEnrollmentsTable(connection);

            /*
             * 初期成績データを登録する。
             */
            GradeDefTable.insertDefaultGradeDefinitions(connection);

            GradeDefInit.initialize();

            /*
             * 既存データベースに対するマイグレーション。
             */
            DatabaseMigration.migrate();

            System.out.println("Database init was SUCCESS");
            System.out.println("Database: " + DatabaseManager.getDatabasePath());

        } catch (SQLException e) {

            System.err.println("Database init was FAIL");

            e.printStackTrace();
        }
    }

}
