package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.service.abstracts.AbstractAcademicRecordService;

import java.sql.SQLException;
import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.validateCurriculumId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateSemester;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateYear;

public class GradeService extends AbstractAcademicRecordService {
    private final EnrollmentService enrollmentService;

    public GradeService() {
        enrollmentService = new EnrollmentService();
    }

    public List<GradeDef> getGradeDefs() throws SQLException {
        return gradeDefRepository.findAll();
    }

    public List<Enrollment> getEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getEnrollmentsByCurriculum(Integer curriculumId) {
        validateCurriculumId(curriculumId);
        return enrollmentRepository.findByCurriculumId(curriculumId);
    }

    public List<Enrollment> getEnrollmentsByCurriculumAndYear(Integer curriculumId, int year) {
        validateCurriculumId(curriculumId);
        validateYear(year);
        return enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
    }

    public List<Enrollment> getEnrollmentsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) {
        validateCurriculumId(curriculumId);
        validateYear(year);
        validateSemester(semester);
        return enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
    }

    public List<EnrollmentDisplay> getEnrollmentDisplays() throws SQLException {
        return enrollmentService.getEnrollmentDisplays();
    }

    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculum(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return enrollmentService.getEnrollmentDisplaysByCurriculum(curriculumId);
    }

    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {
        validateCurriculumId(curriculumId);
        validateYear(year);
        return enrollmentService.getEnrollmentDisplaysByCurriculumAndYear(curriculumId, year);
    }

    public void updateGrade(Integer enrollmentId, Integer gradeId) throws SQLException {
        if (enrollmentId == null) {
            throw new IllegalArgumentException(I18n.text("validation.grade.enrollmentIdRequired"));
        }

        if (enrollmentId <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.grade.enrollmentIdInvalid"));
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);

        if (enrollment == null) {
            throw new IllegalArgumentException(I18n.text("validation.grade.enrollmentMissing", enrollmentId));
        }

        if (gradeId != null) {
            if (gradeId <= 0) {
                throw new IllegalArgumentException(I18n.text("validation.grade.idInvalid"));
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {
                throw new IllegalArgumentException(I18n.text("validation.grade.missing", gradeId));
            }
        }

        enrollment.setGradeId(gradeId);
        enrollmentRepository.update(enrollment);
    }

    public void updateGradeByCurriculum(Integer curriculumId, Integer enrollmentId, Integer gradeId) throws SQLException {
        validateCurriculumId(curriculumId);

        if (enrollmentId == null) {
            throw new IllegalArgumentException(I18n.text("validation.grade.enrollmentIdRequired"));
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);

        if (enrollment == null) {
            throw new IllegalArgumentException(I18n.text("validation.grade.enrollmentMissing", enrollmentId));
        }

        if (!curriculumId.equals(enrollment.getCurriculumId())) {
            throw new IllegalArgumentException(I18n.text("validation.grade.wrongCurriculum"));
        }

        updateGrade(enrollmentId, gradeId);
    }

    public GradeDef getGradeDef(Integer gradeId) throws SQLException {
        if (gradeId == null) {
            return null;
        }

        return gradeDefRepository.findById(gradeId);
    }

}
