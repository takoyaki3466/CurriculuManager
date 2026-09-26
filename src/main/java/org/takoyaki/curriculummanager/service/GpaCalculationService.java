package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.service.abstracts.AbstractAcademicRecordService;

import java.sql.SQLException;
import java.util.List;

public class GpaCalculationService extends AbstractAcademicRecordService {

    public double calculateGpa() throws SQLException {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return calculateGpa(enrollments);
    }

    public double calculateGpa(List<Enrollment> enrollments) throws SQLException {
        if (enrollments == null || enrollments.isEmpty()) {
            return 0.0;
        }

        double totalPoints = 0.0;
        double totalCredits = 0.0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment == null) {
                continue;
            }

            Integer gradeId = enrollment.getGradeId();

            if (gradeId == null) {
                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {
                continue;
            }

            if (!grade.isIncludedInGpa()) {
                continue;
            }

            if (grade.getGpaPoints() == null) {
                continue;
            }

            Integer courseId = enrollment.getCourseId();

            if (courseId == null) {
                continue;
            }

            Course course = courseRepository.findById(courseId);

            if (course == null) {
                continue;
            }

            double credits = course.getCredits();

            if (credits <= 0) {
                continue;
            }

            totalPoints += credits * grade.getGpaPoints();
            totalCredits += credits;
        }

        if (totalCredits <= 0) {
            return 0.0;
        }

        return totalPoints / totalCredits;
    }

    public double calculateGpaByCurriculum(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);
        return calculateGpa(enrollments);
    }

    public double calculateGpaByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {
        validateCurriculumId(curriculumId);
        validateYear(year);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
        return calculateGpa(enrollments);
    }

    public double calculateGpaByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) throws SQLException {
        validateCurriculumId(curriculumId);
        validateYear(year);
        validateSemester(semester);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
        return calculateGpa(enrollments);
    }

    public double calculateGpaByYear(int year) throws SQLException {
        validateYear(year);
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year).toList();
        return calculateGpa(enrollments);
    }

    public double calculateGpaByYearAndSemester(int year, String semester) throws SQLException {
        validateYear(year);
        validateSemester(semester);
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year && semester.equals(enrollment.getSemester())).toList();
        return calculateGpa(enrollments);
    }

}
