package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final GradeDefRepository gradeDefRepository;

    public EnrollmentService() {
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        gradeDefRepository = new GradeDefRepository();
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getEnrollmentsByCurriculum(Integer curriculumId) {
        if (curriculumId == null) {
            return List.of();
        }

        return enrollmentRepository.findByCurriculumId(curriculumId);
    }

    public List<Enrollment> getEnrollmentsByCurriculumAndYear(Integer curriculumId, int year) {
        if (curriculumId == null) {
            return List.of();
        }

        return enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
    }

    public List<Enrollment> getEnrollmentsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) {
        if (curriculumId == null) {
            return List.of();
        }

        return enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
    }

    public List<EnrollmentDisplay> getEnrollmentDisplays() throws SQLException {
        return createDisplays(enrollmentRepository.findAll());
    }

    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculum(Integer curriculumId) throws SQLException {
        if (curriculumId == null) {
            return List.of();
        }

        return createDisplays(enrollmentRepository.findByCurriculumId(curriculumId));
    }

    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {
        if (curriculumId == null) {
            return List.of();
        }

        return createDisplays(enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year));
    }

    public void addEnrollment(Integer curriculumId, Integer courseId, int year, String semester) throws SQLException {
        if (curriculumId == null) {
            throw new IllegalArgumentException("カリキュラムを指定してください。");
        }

        if (courseId == null) {
            throw new IllegalArgumentException("科目を指定してください。");
        }

        if (semester == null || semester.isBlank()) {
            throw new IllegalArgumentException("学期を指定してください。");
        }

        if (year <= 0) {
            throw new IllegalArgumentException("年度が正しくありません。");
        }

        if (enrollmentRepository.exists(curriculumId, courseId, year, semester)) {
            throw new IllegalArgumentException("同じ科目を同じ年度・学期に" + "重複して登録することはできません。");
        }

        Course course = courseRepository.findById(courseId);

        if (course == null) {
            throw new IllegalArgumentException("指定された科目が存在しません: " + courseId);
        }

        Enrollment enrollment = new Enrollment(curriculumId, courseId, year, semester, null);
        enrollmentRepository.save(enrollment);
    }

    public boolean exists(Integer curriculumId, Integer courseId, int year, String semester) {
        if (curriculumId == null || courseId == null || semester == null) {
            return false;
        }

        return enrollmentRepository.exists(curriculumId, courseId, year, semester);
    }

    public void updateEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("履修情報がnullです。");
        }

        if (enrollment.getId() == null) {
            throw new IllegalArgumentException("履修情報IDが指定されていません。");
        }

        if (enrollment.getCurriculumId() == null) {
            throw new IllegalArgumentException("カリキュラムを指定してください。");
        }

        if (enrollment.getCourseId() == null) {
            throw new IllegalArgumentException("科目を指定してください。");
        }

        if (enrollment.getSemester() == null || enrollment.getSemester().isBlank()) {
            throw new IllegalArgumentException("学期を指定してください。");
        }

        enrollmentRepository.update(enrollment);
    }

    public void deleteEnrollment(Integer enrollmentId) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("履修情報IDが指定されていません。");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);

        if (enrollment == null) {
            throw new IllegalArgumentException("指定された履修情報が存在しません: " + enrollmentId);
        }

        enrollmentRepository.deleteById(enrollmentId);
    }

    private List<EnrollmentDisplay> createDisplays(List<Enrollment> enrollments) throws SQLException {
        List<EnrollmentDisplay> displays = new ArrayList<>();
        List<Course> courses = courseRepository.findAll();
        List<GradeDef> grades = gradeDefRepository.findAll();

        for (Enrollment enrollment : enrollments) {
            Course course = courses.stream().filter(c -> c.getId().equals(enrollment.getCourseId())).findFirst().orElse(null);

            if (course == null) {
                continue;
            }

            GradeDef grade = grades.stream().filter(g -> enrollment.getGradeId() != null && g.getId().equals(enrollment.getGradeId())).findFirst().orElse(null);
            displays.add(new EnrollmentDisplay(enrollment.getId(), enrollment.getYear(), enrollment.getSemester(), course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), grade == null ? null : grade.getId(), grade == null ? null : grade.getSymbol()));
        }

        return displays;
    }
}
