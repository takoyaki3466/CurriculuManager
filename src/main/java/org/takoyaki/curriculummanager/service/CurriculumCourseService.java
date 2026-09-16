package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.CurriculumCourseDisplay;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.CurriculumCourseRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * カリキュラムと科目の関連を管理するサービス。
 *
 * <p>
 * CurriculumCourse テーブルを介して、
 * </p>
 *
 * <pre>
 *
 * カリキュラム
 * ```
 * ↓
 * ```
 * CurriculumCourse
 * ```
 * ↓
 * ```
 * 科目
 * </pre>
 *
 * <p>
 * という関係を扱います。
 * </p>
 */
public class CurriculumCourseService {
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final CourseRepository courseRepository;

    /**
     * コンストラクタ。
     */
    public CurriculumCourseService() {

        curriculumCourseRepository = new CurriculumCourseRepository();

        courseRepository = new CourseRepository();
    }

    /**
     * 指定したカリキュラムに登録されている
     * 科目との関連情報を取得します。
     *
     * @param curriculumId カリキュラムID
     * @return カリキュラムと科目の関連一覧
     */
    public List<CurriculumCourse> getCurriculumCourses(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return curriculumCourseRepository.findByCurriculumId(curriculumId);
    }

    /**
     * 指定したカリキュラムに登録されている
     * 科目IDの一覧を取得します。
     *
     * @param curriculumId カリキュラムID
     * @return 科目ID一覧
     */
    public List<Integer> getCourseIds(Integer curriculumId) throws SQLException {

        return getCurriculumCourses(curriculumId).stream().map(CurriculumCourse::getCourseId).filter(id -> id != null).toList();
    }

    /**
     * 指定したカリキュラムに登録されている
     * 科目一覧を取得します。
     *
     * @param curriculumId カリキュラムID
     * @return 科目一覧
     */
    public List<Course> getCourses(Integer curriculumId) throws SQLException {

        List<Integer> courseIds = getCourseIds(curriculumId);

        List<Course> courses = new ArrayList<>();

        for (Integer courseId : courseIds) {

            Course course = courseRepository.findById(courseId);

            if (course != null) {

                courses.add(course);
            }
        }

        return courses;
    }

    /**
     * 指定したカテゴリに所属する科目を、
     * カリキュラム科目設定画面用の
     * 表示モデルへ変換して取得します。
     *
     * <p>
     * このメソッドでは、
     * カテゴリに存在するすべての科目を取得します。
     * </p>
     *
     * <p>
     * すでにカリキュラムへ登録されている科目には
     * 「必修」や「選択」などの区分が設定されます。
     * </p>
     *
     * <p>
     * まだ登録されていない科目の場合は、
     * requirementType が null になります。
     * </p>
     *
     * <pre>
     * カテゴリ
     *     ↓
     * Course
     *     ↓
     * CurriculumCourse
     *     ↓
     * CurriculumCourseDisplay
     * </pre>
     *
     * @param curriculumId カリキュラムID
     * @param categoryId   カテゴリID
     * @return カリキュラム科目設定画面用の科目一覧
     */
    public List<CurriculumCourseDisplay> getCourseDisplaysByCategory(Integer curriculumId, Integer categoryId) throws SQLException {

        validateCurriculumId(curriculumId);

        if (categoryId == null) {

            throw new IllegalArgumentException("カテゴリIDが指定されていません。");
        }

        if (categoryId <= 0) {

            throw new IllegalArgumentException("カテゴリIDが不正です。");
        }

        /*
         * 指定されたカテゴリに所属する
         * 科目を取得します。
         */
        List<Course> courses = courseRepository.findByCategoryId(categoryId);

        /*
         * 現在のカリキュラムに登録されている
         * 科目との関連情報を取得します。
         *
         * 毎回Repositoryへ問い合わせるのではなく、
         * 最初に一覧を取得しておきます。
         */
        List<CurriculumCourse> curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);

        List<CurriculumCourseDisplay> displays = new ArrayList<>();

        for (Course course : courses) {

            if (course == null) {
                continue;
            }

            /*
             * この科目が現在のカリキュラムに
             * 登録されているか探します。
             */
            CurriculumCourse relation = findRelation(curriculumCourses, course.getId());

            /*
             * 登録されていない場合は、
             * requirementType = null とします。
             */
            String requirementType = null;

            Integer relationId = null;

            if (relation != null) {

                relationId = relation.getId();

                requirementType = relation.getRequirementType();
            }

            /*
             * Courseから画面表示用の情報を作ります。
             */
            displays.add(new CurriculumCourseDisplay(relationId, curriculumId, course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), course.getCategoryId(), requirementType));
        }

        return displays;
    }

    /**
     * 現在のカリキュラムに登録されている
     * 科目との関連を一覧から検索します。
     *
     * @param curriculumCourses 関連一覧
     * @param courseId          科目ID
     * @return 該当する関連情報。
     * 存在しない場合はnull。
     */
    private CurriculumCourse findRelation(List<CurriculumCourse> curriculumCourses, Integer courseId) {

        if (courseId == null) {

            return null;
        }

        for (CurriculumCourse curriculumCourse : curriculumCourses) {

            if (curriculumCourse == null) {
                continue;
            }

            if (courseId.equals(curriculumCourse.getCourseId())) {

                return curriculumCourse;
            }
        }

        return null;
    }

    /**
     * 指定したカリキュラムに
     * 指定した科目が登録されているか確認します。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     * @return 登録されていればtrue
     */
    public boolean exists(Integer curriculumId, Integer courseId) throws SQLException {

        validateCurriculumId(curriculumId);

        validateCourseId(courseId);

        return curriculumCourseRepository.findByCurriculumIdAndCourseId(curriculumId, courseId) != null;
    }

    /**
     * カリキュラムに科目を登録します。
     *
     * @param curriculumId    カリキュラムID
     * @param courseId        科目ID
     * @param requirementType 必修・選択などの要件種別
     */
    public void addCourseToCurriculum(Integer curriculumId, Integer courseId, String requirementType) throws SQLException {

        validateCurriculumId(curriculumId);

        validateCourseId(courseId);

        validateRequirementType(requirementType);

        /*
         * 実際に存在する科目か確認します。
         */
        Course course = courseRepository.findById(courseId);

        if (course == null) {

            throw new IllegalArgumentException("科目が存在しません: " + courseId);
        }

        /*
         * 同じ科目が同じカリキュラムに
         * 既に登録されている場合は追加しません。
         */
        if (exists(curriculumId, courseId)) {

            throw new IllegalArgumentException("この科目は既にカリキュラムへ登録されています。");
        }

        CurriculumCourse curriculumCourse = new CurriculumCourse(curriculumId, courseId, requirementType);

        curriculumCourseRepository.save(curriculumCourse);
    }

    /**
     * カリキュラムと科目の関連情報を更新します。
     *
     * <p>
     * 主に「必修」から「選択」、
     * 「選択」から「必修」へ変更する場合に使用します。
     * </p>
     *
     * @param curriculumCourse 更新対象
     */
    public void update(CurriculumCourse curriculumCourse) throws SQLException {

        if (curriculumCourse == null) {

            throw new IllegalArgumentException("カリキュラム科目情報がnullです。");
        }

        if (curriculumCourse.getId() == null) {

            throw new IllegalArgumentException("更新対象のIDが指定されていません。");
        }

        validateCurriculumId(curriculumCourse.getCurriculumId());

        validateCourseId(curriculumCourse.getCourseId());

        validateRequirementType(curriculumCourse.getRequirementType());

        curriculumCourseRepository.update(curriculumCourse);
    }

    /**
     * カリキュラムと科目の関連を削除します。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     */
    public void removeCourseFromCurriculum(Integer curriculumId, Integer courseId) throws SQLException {

        validateCurriculumId(curriculumId);

        validateCourseId(courseId);

        if (!exists(curriculumId, courseId)) {

            throw new IllegalArgumentException("この科目はカリキュラムに登録されていません。");
        }

        curriculumCourseRepository.deleteByCurriculumIdAndCourseId(curriculumId, courseId);
    }

    /**
     * カリキュラム科目情報をIDで削除します。
     *
     * @param id CurriculumCourseのID
     */
    public void delete(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("削除対象のIDが指定されていません。");
        }

        if (curriculumCourseRepository.findById(id) == null) {

            throw new IllegalArgumentException("カリキュラム科目情報が存在しません: " + id);
        }

        curriculumCourseRepository.deleteById(id);
    }

    /**
     * 必修・選択などの要件種別を検証します。
     *
     * <p>
     * 現在の画面では、
     * </p>
     *
     * <ul>
     *     <li>必修</li>
     *     <li>選択</li>
     * </ul>
     *
     * <p>
     * を使用する予定です。
     * </p>
     *
     * <p>
     * DB上ではTEXTとして保存するため、
     * Serviceでは空文字だけを禁止します。
     * </p>
     *
     * @param requirementType 要件種別
     */
    private void validateRequirementType(String requirementType) {

        if (requirementType == null || requirementType.isBlank()) {

            throw new IllegalArgumentException("要件種別を入力してください。");
        }
    }

    /**
     * カリキュラムIDを検証します。
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
     * 科目IDを検証します。
     */
    private void validateCourseId(Integer courseId) {

        if (courseId == null) {

            throw new IllegalArgumentException("科目IDが指定されていません。");
        }

        if (courseId <= 0) {

            throw new IllegalArgumentException("科目IDが不正です。");
        }
    }
}
