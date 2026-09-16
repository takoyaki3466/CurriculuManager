package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * 修得単位数を計算するサービス。
 *
 * <p>
 * 成績が「合格」と定義されている履修について、
 * 科目の単位数を修得単位として加算する。
 * </p>
 *
 * <p>
 * カリキュラムを指定した計算にも対応している。
 * </p>
 */
public class CreditCalculationService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final GradeDefRepository gradeDefRepository;

    public CreditCalculationService() {
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        gradeDefRepository = new GradeDefRepository();
    }

    /**
     * 全履修を対象に修得単位数を計算する。
     *
     * @return 修得単位数
     */
    public double calculateEarnedCredits() throws SQLException {

        List<Enrollment> enrollments = enrollmentRepository.findAll();

        return calculateEarnedCredits(enrollments);
    }

    /**
     * 指定された履修情報から修得単位数を計算する。
     *
     * @param enrollments 履修情報
     * @return 修得単位数
     */
    public double calculateEarnedCredits(List<Enrollment> enrollments) throws SQLException {

        if (enrollments == null || enrollments.isEmpty()) {

            return 0.0;
        }

        double earnedCredits = 0.0;

        for (Enrollment enrollment : enrollments) {

            if (enrollment == null) {
                continue;
            }

            /*
             * 成績が設定されていない履修は、
             * まだ修得単位として扱わない。
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
             * 合格していない成績は
             * 修得単位に含めない。
             *
             * 例:
             * F → 不合格なので除外
             * A → 合格なので加算
             * P → 合格なので加算
             */
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

            /*
             * 0以下の単位数は
             * 不正なデータとして除外する。
             */
            if (credits <= 0) {
                continue;
            }

            earnedCredits += credits;
        }

        return earnedCredits;
    }

    /**
     * 指定したカリキュラムの履修だけを対象に
     * 修得単位数を計算する。
     *
     * @param curriculumId カリキュラムID
     * @return 修得単位数
     */
    public double calculateEarnedCreditsByCurriculum(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        return calculateEarnedCredits(enrollments);
    }

    /**
     * 指定したカリキュラム・年度の履修だけを対象に
     * 修得単位数を計算する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return 修得単位数
     */
    public double calculateEarnedCreditsByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {

        validateCurriculumId(curriculumId);

        validateYear(year);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);

        return calculateEarnedCredits(enrollments);
    }

    /**
     * 指定したカリキュラム・年度・学期の履修だけを対象に
     * 修得単位数を計算する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @param semester     学期
     * @return 修得単位数
     */
    public double calculateEarnedCreditsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) throws SQLException {

        validateCurriculumId(curriculumId);

        validateYear(year);

        validateSemester(semester);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);

        return calculateEarnedCredits(enrollments);
    }

    /**
     * 指定した年度の履修だけを対象に
     * 修得単位数を計算する。
     *
     * @param year 年度
     * @return 修得単位数
     */
    public double calculateEarnedCreditsByYear(int year) throws SQLException {

        validateYear(year);

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year).toList();

        return calculateEarnedCredits(enrollments);
    }

    /**
     * 指定した年度・学期の履修だけを対象に
     * 修得単位数を計算する。
     *
     * @param year     年度
     * @param semester 学期
     * @return 修得単位数
     */
    public double calculateEarnedCreditsByYearAndSemester(int year, String semester) throws SQLException {

        validateYear(year);

        validateSemester(semester);

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getYear() == year && semester.equals(enrollment.getSemester())).toList();

        return calculateEarnedCredits(enrollments);
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
