package org.takoyaki.curriculummanager.service.abstracts;

import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;

public abstract class AbstractAcademicRecordService {
    protected final EnrollmentRepository enrollmentRepository;
    protected final CourseRepository courseRepository;
    protected final GradeDefRepository gradeDefRepository;

    protected AbstractAcademicRecordService() {
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        gradeDefRepository = new GradeDefRepository();
    }

    protected void validateCurriculumId(Integer curriculumId) {
        if (curriculumId == null) {
            throw new IllegalArgumentException("カリキュラムIDが指定されていません。");
        }

        if (curriculumId <= 0) {
            throw new IllegalArgumentException("カリキュラムIDが不正です。");
        }
    }

    protected void validateYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("年度が不正です。");
        }
    }

    protected void validateSemester(String semester) {
        if (semester == null || semester.isBlank()) {
            throw new IllegalArgumentException("学期が指定されていません。");
        }
    }
}
