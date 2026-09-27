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

}
