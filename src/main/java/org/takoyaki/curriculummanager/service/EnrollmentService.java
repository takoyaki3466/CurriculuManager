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

/**
 * 履修情報に関する業務処理を担当するService。
 * <p>
 * GUIから直接Repositoryを操作せず、
 * 履修登録・重複確認・表示用データ作成などを
 * このクラスに集約する。
 */
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final GradeDefRepository gradeDefRepository;

    /**
     * Serviceを生成する。
     */
    public EnrollmentService() {
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        gradeDefRepository = new GradeDefRepository();
    }

    /**
     * すべての履修情報を取得する。
     *
     * @return 履修情報一覧
     */
    public List<Enrollment> getAllEnrollments() {

        return enrollmentRepository.findAll();
    }

    /**
     * 指定したカリキュラムの履修情報を取得する。
     *
     * @param curriculumId カリキュラムID
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollmentsByCurriculum(Integer curriculumId) {

        if (curriculumId == null) {
            return List.of();
        }

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

        if (curriculumId == null) {
            return List.of();
        }

        return enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year);
    }

    /**
     * 指定したカリキュラム・年度・学期の
     * 履修情報を取得する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @param semester     学期
     * @return 履修情報一覧
     */
    public List<Enrollment> getEnrollmentsByCurriculumAndYearAndSemester(Integer curriculumId, int year, String semester) {

        if (curriculumId == null) {
            return List.of();
        }

        return enrollmentRepository.findByCurriculumIdAndYearAndSemester(curriculumId, year, semester);
    }

    /**
     * 履修情報を画面表示用の形式へ変換する。
     *
     * @return 履修情報表示一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplays() throws SQLException {

        return createDisplays(enrollmentRepository.findAll());
    }

    /**
     * 指定したカリキュラムの履修情報を
     * 画面表示用の形式へ変換する。
     *
     * @param curriculumId カリキュラムID
     * @return 履修情報表示一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculum(Integer curriculumId) throws SQLException {

        if (curriculumId == null) {
            return List.of();
        }

        return createDisplays(enrollmentRepository.findByCurriculumId(curriculumId));
    }

    /**
     * 指定したカリキュラム・年度の履修情報を
     * 画面表示用の形式へ変換する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return 履修情報表示一覧
     */
    public List<EnrollmentDisplay> getEnrollmentDisplaysByCurriculumAndYear(Integer curriculumId, int year) throws SQLException {

        if (curriculumId == null) {
            return List.of();
        }

        return createDisplays(enrollmentRepository.findByCurriculumIdAndYear(curriculumId, year));
    }

    /**
     * 新しい履修情報を登録する。
     * <p>
     * 成績は登録時点では未入力にする。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     * @param year         年度
     * @param semester     学期
     */
    public void addEnrollment(Integer curriculumId, Integer courseId, int year, String semester) throws SQLException {

        /*
         * カリキュラムが指定されていない状態では
         * 履修登録できないようにする。
         */
        if (curriculumId == null) {

            throw new IllegalArgumentException("カリキュラムを指定してください。");
        }

        /*
         * 科目が指定されていない場合。
         */
        if (courseId == null) {

            throw new IllegalArgumentException("科目を指定してください。");
        }

        /*
         * 学期が空の場合。
         */
        if (semester == null || semester.isBlank()) {

            throw new IllegalArgumentException("学期を指定してください。");
        }

        /*
         * 年度の簡単な入力チェック。
         */
        if (year <= 0) {

            throw new IllegalArgumentException("年度が正しくありません。");
        }

        /*
         * 同じカリキュラム・科目・年度・学期の
         * 履修が既に存在しないか確認する。
         */
        if (enrollmentRepository.exists(curriculumId, courseId, year, semester)) {

            throw new IllegalArgumentException("同じ科目を同じ年度・学期に" + "重複して登録することはできません。");
        }

        /*
         * 科目が実際に存在するか確認する。
         */
        Course course = courseRepository.findById(courseId);

        if (course == null) {

            throw new IllegalArgumentException("指定された科目が存在しません: " + courseId);
        }

        /*
         * 成績未入力でEnrollmentを作成する。
         */
        Enrollment enrollment = new Enrollment(curriculumId, courseId, year, semester, null);

        enrollmentRepository.save(enrollment);
    }

    /**
     * 履修情報が既に存在するか確認する。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     * @param year         年度
     * @param semester     学期
     * @return 存在する場合true
     */
    public boolean exists(Integer curriculumId, Integer courseId, int year, String semester) {

        if (curriculumId == null || courseId == null || semester == null) {

            return false;
        }

        return enrollmentRepository.exists(curriculumId, courseId, year, semester);
    }

    /**
     * 履修情報を更新する。
     * <p>
     * 主に年度・学期・科目・カリキュラムなどの
     * 編集に使用する。
     *
     * @param enrollment 更新する履修情報
     */
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

    /**
     * 履修情報を削除する。
     *
     * @param enrollmentId 履修情報ID
     */
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

    /**
     * Enrollment一覧をEnrollmentDisplay一覧へ変換する。
     * <p>
     * 科目名・科目コード・単位数・成績記号などは、
     * Enrollment自身には保持していないため、
     * Repositoryから関連データを取得して組み立てる。
     */
    private List<EnrollmentDisplay> createDisplays(List<Enrollment> enrollments) throws SQLException {

        List<EnrollmentDisplay> displays = new ArrayList<>();

        /*
         * 同じ科目を何度も取得しないように、
         * 簡易的なキャッシュを作る。
         */
        List<Course> courses = courseRepository.findAll();

        List<GradeDef> grades = gradeDefRepository.findAll();

        for (Enrollment enrollment : enrollments) {

            /*
             * 科目を検索する。
             */
            Course course = courses.stream().filter(c -> c.getId().equals(enrollment.getCourseId())).findFirst().orElse(null);

            /*
             * 科目が削除されているなど、
             * 関連データが存在しない場合は
             * 画面表示から除外する。
             */
            if (course == null) {
                continue;
            }

            /*
             * 成績を検索する。
             *
             * 成績未入力の場合はnullになる。
             */
            GradeDef grade = grades.stream().filter(g -> enrollment.getGradeId() != null && g.getId().equals(enrollment.getGradeId())).findFirst().orElse(null);

            displays.add(new EnrollmentDisplay(enrollment.getId(), enrollment.getYear(), enrollment.getSemester(), course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), grade == null ? null : grade.getId(), grade == null ? null : grade.getSymbol()));
        }

        return displays;
    }
}
