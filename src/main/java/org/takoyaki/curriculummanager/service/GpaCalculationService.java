package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;
import org.takoyaki.curriculummanager.repository.CourseRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * GPAを計算するサービス。
 *
 * <p>
 * GPAは、
 * <p>
 * Σ（単位数 × GPAポイント）
 * -----------------------
 * GPA対象単位数
 * <p>
 * で計算する。
 * </p>
 *
 * <p>
 * このクラスでは、通常の全履修を対象とした計算に加えて、
 * 特定のカリキュラムだけを対象にした計算にも対応する。
 * </p>
 */
public class GpaCalculationService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final GradeDefRepository gradeDefRepository;

    public GpaCalculationService() {
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        gradeDefRepository = new GradeDefRepository();
    }

    /**
     * 全履修を対象にGPAを計算する。
     *
     * @return GPA
     */
    public double calculateGpa() throws SQLException {

        List<Enrollment> enrollments = enrollmentRepository.findAll();

        return calculateGpa(enrollments);
    }

    /**
     * 指定された履修情報からGPAを計算する。
     *
     * @param enrollments 履修情報
     * @return GPA
     */
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

            /*
             * 成績が設定されていない履修は
             * GPA計算の対象外。
             */
            Integer gradeId = enrollment.getGradeId();

            if (gradeId == null) {
                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {
                continue;
            }

            /*
             * GPA対象外の成績。
             *
             * 例えばP（合格）は単位としては認定するが、
             * GPAには含めない。
             */
            if (!grade.isIncludedInGpa()) {
                continue;
            }

            /*
             * GPAポイントが存在しない成績は
             * GPA計算できないためスキップする。
             */
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

            /*
             * 不正な単位数を計算に含めない。
             */
            if (credits <= 0) {
                continue;
            }

            /*
             * GPAポイント × 単位数
             */
            totalPoints += credits * grade.getGpaPoints();

            /*
             * GPA対象単位数
             */
            totalCredits += credits;
        }

        /*
         * GPA対象科目が存在しない場合。
         */
        if (totalCredits <= 0) {
            return 0.0;
        }

        return totalPoints / totalCredits;
    }

    /**
     * 指定したカリキュラムの履修だけを対象にGPAを計算する。
     *
     * @param curriculumId カリキュラムID
     * @return GPA
     */
    public double calculateGpaByCurriculum(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        return calculateGpa(enrollments);
    }

    /**
     * 指定したカリキュラム・年度の履修だけを対象に
     * GPAを計算する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return GPA
     */
    public double calculateGpaByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {

        validateCurriculumId(curriculumId);

        validateYear(year);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);

        return calculateGpa(enrollments);
    }

    /**
     * 指定したカリキュラム・年度・学期の履修だけを
     * 対象にGPAを計算する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @param semester     学期
     * @return GPA
     */
    public double calculateGpaByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) throws SQLException {

        validateCurriculumId(curriculumId);

        validateYear(year);

        validateSemester(semester);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);

        return calculateGpa(enrollments);
    }

    /**
     * 指定した年度の履修だけを対象にGPAを計算する。
     *
     * @param year 年度
     * @return GPA
     */
    public double calculateGpaByYear(int year) throws SQLException {

        validateYear(year);

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year).toList();

        return calculateGpa(enrollments);
    }

    /**
     * 指定した年度・学期の履修だけを対象に
     * GPAを計算する。
     *
     * @param year     年度
     * @param semester 学期
     * @return GPA
     */
    public double calculateGpaByYearAndSemester(int year, String semester) throws SQLException {

        validateYear(year);

        validateSemester(semester);

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year && semester.equals(enrollment.getSemester())).toList();

        return calculateGpa(enrollments);
    }

    /**
     * カリキュラムIDを検証する。
     */
    private void validateCurriculumId(Integer curriculumId) {

        if (curriculumId == null) {

            throw new IllegalArgumentException("カリキュラムIDが指定されていません。");
        }

        if (curriculumId <= 0) {

            throw new IllegalArgumentException("カリキュラムIDが不正です。");
        }
    }

    /**
     * 年度を検証する。
     */
    private void validateYear(int year) {

        if (year <= 0) {

            throw new IllegalArgumentException("年度が不正です。");
        }
    }

    /**
     * 学期を検証する。
     */
    private void validateSemester(String semester) {

        if (semester == null || semester.isBlank()) {

            throw new IllegalArgumentException("学期が指定されていません。");
        }
    }
}