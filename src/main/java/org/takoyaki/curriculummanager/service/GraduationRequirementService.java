package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.GraduationRequirementDisplay;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;
import org.takoyaki.curriculummanager.repository.CourseCategoryRepository;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.CurriculumCourseRepository;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;
import org.takoyaki.curriculummanager.repository.GraduationRequirementRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 卒業要件に関する処理を管理するサービス。
 *
 * <p>
 * 卒業要件について、
 * </p>
 *
 * <ul>
 *     <li>必要単位数</li>
 *     <li>修得単位数</li>
 *     <li>残り必要単位数</li>
 *     <li>必修科目の修得状況</li>
 *     <li>卒業要件の対象カテゴリ</li>
 * </ul>
 *
 * <p>
 * などを管理します。
 * </p>
 */
public class GraduationRequirementService {

    private static final String REQUIREMENT_REQUIRED = "必修";

    private final EnrollmentRepository enrollmentRepository;

    private final CourseRepository courseRepository;

    private final CourseCategoryRepository categoryRepository;

    private final GradeDefRepository gradeDefRepository;

    private final GraduationRequirementRepository graduationRequirementRepository;

    private final CurriculumCourseRepository curriculumCourseRepository;

    public GraduationRequirementService() {

        enrollmentRepository = new EnrollmentRepository();

        courseRepository = new CourseRepository();

        categoryRepository = new CourseCategoryRepository();

        gradeDefRepository = new GradeDefRepository();

        graduationRequirementRepository = new GraduationRequirementRepository();

        curriculumCourseRepository = new CurriculumCourseRepository();
    }

    /**
     * カリキュラムが卒業要件を
     * すべて満たしているか確認します。
     *
     * <p>
     * 以下の両方を満たす必要があります。
     * </p>
     *
     * <ul>
     *     <li>設定されたすべての卒業要件を満たす</li>
     *     <li>設定された必修科目をすべて修得する</li>
     * </ul>
     */
    public boolean isGraduated(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return areRequirementsSatisfied(curriculumId)

                && isMandatoryCoursesSatisfied(curriculumId);
    }

    /**
     * 登録されている単位要件をすべて満たしているか確認します。
     *
     * <p>卒業要件が1件も登録されていない場合は、達成とは扱いません。</p>
     */
    public boolean areRequirementsSatisfied(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        List<GraduationRequirement> requirements = graduationRequirementRepository.findByCurriculumId(curriculumId);

        if (requirements.isEmpty()) {

            return false;
        }

        for (GraduationRequirement requirement : requirements) {

            if (!isRequirementSatisfied(requirement)) {

                return false;
            }
        }

        return true;
    }

    /**
     * 卒業単位要件が登録されているか確認します。
     */
    public boolean hasRequirements(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return !graduationRequirementRepository.findByCurriculumId(curriculumId).isEmpty();
    }

    /**
     * 必修科目が設定されているか確認します。
     */
    public boolean hasMandatoryCourses(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return curriculumCourseRepository.findByCurriculumId(curriculumId).stream()

                .anyMatch(curriculumCourse -> isRequired(curriculumCourse.getRequirementType()));
    }

    /**
     * カリキュラムに設定された必修科目を
     * すべて修得しているか確認します。
     */
    public boolean isMandatoryCoursesSatisfied(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        var curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        Set<Integer> passedCourseIds = getPassedCourseIds(enrollments);

        for (var curriculumCourse : curriculumCourses) {

            if (!isRequired(curriculumCourse.getRequirementType())) {

                continue;
            }

            Integer courseId = curriculumCourse.getCourseId();

            if (!passedCourseIds.contains(courseId)) {

                return false;
            }
        }

        return true;
    }

    /**
     * カリキュラムに設定された
     * 必修科目の修得状況を取得します。
     */
    public List<MandatoryCourseStatus> getMandatoryCourseStatuses(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        var curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        Set<Integer> passedCourseIds = getPassedCourseIds(enrollments);

        List<MandatoryCourseStatus> statuses = new ArrayList<>();

        for (var curriculumCourse : curriculumCourses) {

            if (!isRequired(curriculumCourse.getRequirementType())) {

                continue;
            }

            Integer courseId = curriculumCourse.getCourseId();

            var course = courseRepository.findById(courseId);

            if (course == null) {

                continue;
            }

            boolean passed = passedCourseIds.contains(courseId);

            statuses.add(new MandatoryCourseStatus(course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), passed));
        }

        return statuses;
    }

    private boolean isRequired(String requirementType) {

        return requirementType != null

                && REQUIREMENT_REQUIRED.equals(requirementType.trim());
    }

    /**
     * 指定した卒業要件を満たしているか確認します。
     */
    private boolean isRequirementSatisfied(GraduationRequirement requirement) throws SQLException {

        double earnedCredits = getEarnedCredits(requirement);

        return earnedCredits >= requirement.getRequiredCredits();
    }

    /**
     * 卒業要件に対する修得単位数を取得します。
     *
     * <p>
     * カテゴリが設定されていない場合は
     * カリキュラム全体を対象にします。
     * </p>
     *
     * <p>
     * 複数カテゴリが設定されている場合は、
     * 選択されたすべてのカテゴリと
     * その子カテゴリをまとめて対象にします。
     * </p>
     */
    public double getEarnedCredits(GraduationRequirement requirement) throws SQLException {

        if (requirement == null) {

            throw new IllegalArgumentException("卒業要件が指定されていません。");
        }

        validateCurriculumId(requirement.getCurriculumId());

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(requirement.getCurriculumId());

        /*
         * 一度でも合格した科目のIDを取得します。
         */
        Set<Integer> passedCourseIds = getPassedCourseIds(enrollments);

        /*
         * 卒業要件の対象カテゴリと、
         * その子カテゴリをすべて取得します。
         */
        Set<Integer> targetCategoryIds = collectTargetCategoryIds(requirement.getCategoryIds());

        boolean allCategories = requirement.isAllCategories();

        /*
         * 二重計上防止。
         */
        Set<Integer> countedCourseIds = new HashSet<>();

        double earnedCredits = 0.0;

        for (Integer courseId : passedCourseIds) {

            if (!countedCourseIds.add(courseId)) {

                continue;
            }

            var course = courseRepository.findById(courseId);

            if (course == null) {

                continue;
            }

            /*
             * 「すべて」の場合。
             */
            if (allCategories) {

                earnedCredits += course.getCredits();

                continue;
            }

            Integer courseCategoryId = course.getCategoryId();

            /*
             * カテゴリ未設定の科目は、
             * カテゴリ指定要件には含めません。
             */
            if (courseCategoryId == null) {

                continue;
            }

            if (targetCategoryIds.contains(courseCategoryId)) {

                earnedCredits += course.getCredits();
            }
        }

        return earnedCredits;
    }

    /**
     * 複数の対象カテゴリと、
     * その子カテゴリをすべて取得します。
     */
    private Set<Integer> collectTargetCategoryIds(List<Integer> selectedCategoryIds) throws SQLException {

        Set<Integer> categoryIds = new HashSet<>();

        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {

            return categoryIds;
        }

        for (Integer categoryId : selectedCategoryIds) {

            collectCategoryIds(categoryId, categoryIds);
        }

        return categoryIds;
    }

    /**
     * 指定カテゴリと子カテゴリを
     * 再帰的に取得します。
     */
    private void collectCategoryIds(Integer categoryId, Set<Integer> categoryIds) throws SQLException {

        if (categoryId == null) {

            return;
        }

        /*
         * すでに探索済みなら終了します。
         */
        if (!categoryIds.add(categoryId)) {

            return;
        }

        List<CourseCategory> children = categoryRepository.findByParentId(categoryId);

        for (CourseCategory child : children) {

            collectCategoryIds(child.getId(), categoryIds);
        }
    }

    /**
     * 履修情報から、
     * 一度でも合格した科目IDを取得します。
     */
    private Set<Integer> getPassedCourseIds(List<Enrollment> enrollments) throws SQLException {

        Set<Integer> passedCourseIds = new HashSet<>();

        for (Enrollment enrollment : enrollments) {

            Integer gradeId = enrollment.getGradeId();

            if (gradeId == null) {

                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null || !grade.isPassed()) {

                continue;
            }

            passedCourseIds.add(enrollment.getCourseId());
        }

        return passedCourseIds;
    }

    /**
     * 残り必要単位数を取得します。
     */
    public double getRemainingCredits(GraduationRequirement requirement) throws SQLException {

        double earnedCredits = getEarnedCredits(requirement);

        return Math.max(0.0, requirement.getRequiredCredits() - earnedCredits);
    }

    /**
     * カリキュラムの卒業要件一覧を取得します。
     */
    public List<GraduationRequirement> getRequirements(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return graduationRequirementRepository.findByCurriculumId(curriculumId);
    }

    /**
     * 卒業要件の表示用データを取得します。
     *
     * <p>
     * 対象カテゴリについて、
     * </p>
     *
     * <pre>
     * すべて
     *
     * 一般教養
     *
     * 数学 / 物理 / 化学
     * </pre>
     *
     * <p>
     * のような表示文字列を生成します。
     * </p>
     */
    public List<GraduationRequirementDisplay> getRequirementDisplays(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        List<GraduationRequirement> requirements = getRequirements(curriculumId);

        List<GraduationRequirementDisplay> displays = new ArrayList<>();

        for (GraduationRequirement requirement : requirements) {

            /*
             * 修得単位数。
             */
            double earned = getEarnedCredits(requirement);

            /*
             * 表示する対象カテゴリ名。
             */
            String targetCategories = getTargetCategoriesText(requirement);

            /*
             * 新しい4引数コンストラクタを使用します。
             *
             * name
             * targetCategories
             * requiredCredits
             * earnedCredits
             */
            displays.add(new GraduationRequirementDisplay(requirement.getId(), requirement.getName(), targetCategories, requirement.getRequiredCredits(), earned));
        }

        return displays;
    }

    /**
     * 卒業要件の対象カテゴリを
     * 画面表示用の文字列へ変換します。
     *
     * <p>
     * カテゴリなし:
     * </p>
     *
     * <pre>
     * すべて
     * </pre>
     *
     * <p>
     * 1カテゴリ:
     * </p>
     *
     * <pre>
     * 一般教養
     * </pre>
     *
     * <p>
     * 複数カテゴリ:
     * </p>
     *
     * <pre>
     * 数学 / 物理 / 化学
     * </pre>
     */
    private String getTargetCategoriesText(GraduationRequirement requirement) throws SQLException {

        /*
         * categoryIdsが空なら
         * カリキュラム全体です。
         */
        if (requirement.isAllCategories()) {

            return "すべて";
        }

        List<String> categoryNames = new ArrayList<>();

        for (Integer categoryId : requirement.getCategoryIds()) {

            if (categoryId == null) {

                continue;
            }

            CourseCategory category = categoryRepository.findById(categoryId);

            /*
             * カテゴリが削除されているなどして
             * 存在しない場合は表示対象外にします。
             */
            if (category == null) {

                continue;
            }

            categoryNames.add(category.getName());
        }

        /*
         * DB上はカテゴリ指定になっているが、
         * 有効なカテゴリが1件も見つからなかった場合。
         */
        if (categoryNames.isEmpty()) {

            return "カテゴリなし";
        }

        /*
         * JavaのString.joinを使って、
         *
         * 数学 / 物理 / 化学
         *
         * のように連結します。
         */
        return String.join(" / ", categoryNames);
    }

    /**
     * カリキュラムIDを検証します。
     */
    private void validateCurriculumId(Integer curriculumId) {

        if (curriculumId == null) {

            throw new IllegalArgumentException("カリキュラムIDが指定されていません。");
        }
    }
}
