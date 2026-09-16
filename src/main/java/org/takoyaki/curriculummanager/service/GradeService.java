package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * 成績を管理するサービス。
 *
 * <p>
 * 成績定義の取得や、履修情報に対する成績の設定を行う。
 * </p>
 *
 * <p>
 * また、特定のカリキュラムに属する履修だけを
 * 取得することもできる。
 * </p>
 */
public class GradeService {
    private final EnrollmentRepository enrollmentRepository;
    private final GradeDefRepository gradeDefRepository;
    private final EnrollmentService enrollmentService;

    public GradeService() {
        enrollmentRepository = new EnrollmentRepository();
        gradeDefRepository = new GradeDefRepository();
        enrollmentService = new EnrollmentService();
    }

    /**
     * 登録されている成績定義をすべて取得する。
     *
     * @return 成績定義一覧
     */
    public List<GradeDef> getGradeDefs() throws SQLException {

        return gradeDefRepository.findAll();
    }

    /**
     * すべての履修情報を取得する。
     *
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollments() {

        return enrollmentRepository.findAll();
    }

    /**
     * 指定したカリキュラムの履修情報を取得する。
     *
     * @param curriculumId カリキュラムID
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollmentsByCurriculum(Integer curriculumId) {

        validateCurriculumId(curriculumId);

        return enrollmentRepository.findByCurriculumId(curriculumId);
    }

    /**
     * 指定したカリキュラム・年度の履修情報を取得する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollmentsByCurriculumAndYear(Integer curriculumId, int year) {

        validateCurriculumId(curriculumId);

        validateYear(year);

        return enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
    }

    /**
     * 指定したカリキュラム・年度・学期の履修情報を取得する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @param semester     学期
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollmentsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) {

        validateCurriculumId(curriculumId);

        validateYear(year);

        validateSemester(semester);

        return enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
    }

    /**
     * 全履修情報を画面表示用データへ変換する。
     *
     * @return 履修表示情報一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplays() throws SQLException {

        return enrollmentService.getEnrollmentDisplays();
    }

    /**
     * 指定したカリキュラムの履修情報を
     * 画面表示用データへ変換する。
     *
     * @param curriculumId カリキュラムID
     * @return 履修表示情報一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculum(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return enrollmentService.getEnrollmentDisplaysByCurriculum(curriculumId);
    }

    /**
     * 指定したカリキュラム・年度の履修情報を
     * 画面表示用データへ変換する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return 履修表示情報一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {

        validateCurriculumId(curriculumId);

        validateYear(year);

        return enrollmentService.getEnrollmentDisplaysByCurriculumAndYear(curriculumId, year);
    }

    /**
     * 指定した履修に成績を設定する。
     *
     * @param enrollmentId 履修ID
     * @param gradeId      成績定義ID
     */
    public void updateGrade(Integer enrollmentId, Integer gradeId) throws SQLException {

        if (enrollmentId == null) {

            throw new IllegalArgumentException("履修IDが指定されていません。");
        }

        if (enrollmentId <= 0) {

            throw new IllegalArgumentException("履修IDが不正です。");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);

        if (enrollment == null) {

            throw new IllegalArgumentException("履修情報が存在しません: " + enrollmentId);
        }

        /*
         * gradeId == null の場合は、
         * 成績を未設定に戻すことができる。
         */
        if (gradeId != null) {

            if (gradeId <= 0) {

                throw new IllegalArgumentException("成績IDが不正です。");
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {

                throw new IllegalArgumentException("指定された成績が存在しません: " + gradeId);
            }
        }

        enrollment.setGradeId(gradeId);

        enrollmentRepository.update(enrollment);
    }

    /**
     * 指定したカリキュラムの履修に対して
     * 成績を設定する。
     *
     * <p>
     * 更新前に、対象の履修が指定されたカリキュラムに
     * 属していることも確認する。
     * </p>
     *
     * @param curriculumId カリキュラムID
     * @param enrollmentId 履修ID
     * @param gradeId      成績定義ID
     */
    public void updateGradeByCurriculum(Integer curriculumId, Integer enrollmentId, Integer gradeId) throws SQLException {

        validateCurriculumId(curriculumId);

        if (enrollmentId == null) {

            throw new IllegalArgumentException("履修IDが指定されていません。");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);

        if (enrollment == null) {

            throw new IllegalArgumentException("履修情報が存在しません: " + enrollmentId);
        }

        /*
         * 別カリキュラムの履修を
         * 誤って変更しないようにする。
         */
        if (!curriculumId.equals(enrollment.getCurriculumId())) {

            throw new IllegalArgumentException("この履修は指定されたカリキュラムに属していません。");
        }

        updateGrade(enrollmentId, gradeId);
    }

    /**
     * 指定された成績IDから成績定義を取得する。
     *
     * @param gradeId 成績ID
     * @return 成績定義
     */
    public GradeDef getGradeDef(Integer gradeId) throws SQLException {

        if (gradeId == null) {
            return null;
        }

        return gradeDefRepository.findById(gradeId);
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
