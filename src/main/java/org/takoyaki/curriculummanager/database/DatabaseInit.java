package org.takoyaki.curriculummanager.database;

import org.takoyaki.curriculummanager.i18n.I18n;

import org.takoyaki.curriculummanager.database.tables.CourseTable;
import org.takoyaki.curriculummanager.database.tables.AppSettingsTable;
import org.takoyaki.curriculummanager.database.tables.CurriculaTable;
import org.takoyaki.curriculummanager.database.tables.DepartmentsTable;
import org.takoyaki.curriculummanager.database.tables.EnrollmentsTable;
import org.takoyaki.curriculummanager.database.tables.GradeDefTable;
import org.takoyaki.curriculummanager.database.tables.GraduationRequirementCategoriesTable;
import org.takoyaki.curriculummanager.database.tables.GraduationRequirementsTable;
import org.takoyaki.curriculummanager.database.tables.MajorsTable;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseInit {
    private DatabaseInit() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseManager.getConnection()) {
            DepartmentsTable.createDepartmentsTable(connection);
            MajorsTable.createMajorsTable(connection);
            AppSettingsTable.createAppSettingsTable(connection);
            CurriculaTable.createCurriculaTable(connection);
            CourseTable.createCourseCategoriesTable(connection);
            CourseTable.createCoursesTable(connection);
            CurriculaTable.createCurriculumCoursesTable(connection);
            GraduationRequirementsTable.createGraduationRequirementsTable(connection);
            GraduationRequirementCategoriesTable.createGraduationRequirementCategoriesTable(connection);
            GradeDefTable.createGradeDefinitionsTable(connection);
            EnrollmentsTable.createEnrollmentsTable(connection);
            GradeDefTable.insertDefaultGradeDefinitions(connection);
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("database.error.initialize"), e);
        }

        GradeDefInit.initialize();
        DatabaseMigration.migrate();
    }
}
