package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.service.abstracts.AbstractAcademicRecordService;

import java.sql.SQLException;
import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.validateCurriculumId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateSemester;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateYear;

public class CreditCalculationService extends AbstractAcademicRecordService {

    public double calculateEarnedCredits() throws SQLException {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return calculateEarnedCredits(enrollments);
    }

    public double calculateEarnedCredits(List<Enrollment> enrollments) throws SQLException {
        if (enrollments == null || enrollments.isEmpty()) {
            return 0.0;
        }

        double earnedCredits = 0.0;

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

            if (!grade.isPassed()) {
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

            earnedCredits += credits;
        }

        return earnedCredits;
    }

    public double calculateEarnedCreditsByCurriculum(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);
        return calculateEarnedCredits(enrollments);
    }

    public double calculateEarnedCreditsByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {
        validateCurriculumId(curriculumId);
        validateYear(year);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
        return calculateEarnedCredits(enrollments);
    }

    public double calculateEarnedCreditsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) throws SQLException {
        validateCurriculumId(curriculumId);
        validateYear(year);
        validateSemester(semester);
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
        return calculateEarnedCredits(enrollments);
    }

    public double calculateEarnedCreditsByYear(int year) throws SQLException {
        validateYear(year);
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year).toList();
        return calculateEarnedCredits(enrollments);
    }

    public double calculateEarnedCreditsByYearAndSemester(int year, String semester) throws SQLException {
        validateYear(year);
        validateSemester(semester);
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year && semester.equals(enrollment.getSemester())).toList();
        return calculateEarnedCredits(enrollments);
    }

}
