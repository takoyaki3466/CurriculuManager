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
     * カリキュラムが卒業要件を満たしているか確認する。
     *
     * <p>
     * 以下の両方を満たしている必要がある。
     *
     * <ul>
     *     <li>卒業要件の必要単位を満たしている</li>
     *     <li>必修科目をすべて修得している</li>
     * </ul>
     */
    public boolean isGraduated(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        /*
         * 通常の卒業要件を確認する。
         */
        List<GraduationRequirement> requirements = graduationRequirementRepository.findByCurriculumId(curriculumId);

        for (GraduationRequirement requirement : requirements) {

            if (!isRequirementSatisfied(requirement)) {
                return false;
            }
        }

        /*
         * 必修科目を確認する。
         */
        return isMandatoryCoursesSatisfied(curriculumId);
    }

    /**
     * カリキュラムに設定された必修科目を
     * すべて修得しているか確認する。
     */
    public boolean isMandatoryCoursesSatisfied(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        /*
         * カリキュラムに登録されている科目を取得する。
         */
        var curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);

        /*
         * そのカリキュラムの履修記録を取得する。
         */
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        /*
         * 修得済み科目のIDを記録する。
         */
        Set<Integer> passedCourseIds = new HashSet<>();

        for (Enrollment enrollment : enrollments) {

            Integer gradeId = enrollment.getGradeId();

            /*
             * 成績が未設定なら
             * 修得済みではない。
             */
            if (gradeId == null) {
                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {
                continue;
            }

            /*
             * 合格した科目だけを
             * 修得済みとして扱う。
             */
            if (grade.isPassed()) {

                passedCourseIds.add(enrollment.getCourseId());
            }
        }

        /*
         * 必修科目を1つずつ確認する。
         */
        for (var curriculumCourse : curriculumCourses) {

            if (!REQUIREMENT_REQUIRED.equals(curriculumCourse.getRequirementType())) {
                continue;
            }

            Integer courseId = curriculumCourse.getCourseId();

            /*
             * 必修科目が修得済みでなければ、
             * 卒業要件を満たしていない。
             */
            if (!passedCourseIds.contains(courseId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * カリキュラムに設定された必修科目の
     * 修得状況を取得する。
     *
     * <p>
     * 必修科目ごとに、
     * 科目情報と修得済みかどうかをまとめて返す。
     */
    public List<MandatoryCourseStatus> getMandatoryCourseStatuses(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        /*
         * カリキュラムに登録されている科目を取得する。
         */
        var curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);

        /*
         * 履修記録を取得する。
         */
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(curriculumId);

        /*
         * 修得済み科目のIDを作成する。
         *
         * 同じ科目を複数回履修していても、
         * 一度でも合格していれば修得済みとする。
         */
        Set<Integer> passedCourseIds = new HashSet<>();

        for (Enrollment enrollment : enrollments) {

            Integer gradeId = enrollment.getGradeId();

            if (gradeId == null) {
                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null) {
                continue;
            }

            if (grade.isPassed()) {

                passedCourseIds.add(enrollment.getCourseId());
            }
        }

        /*
         * 必修科目の表示用データを作成する。
         */
        List<MandatoryCourseStatus> statuses = new ArrayList<>();

        for (var curriculumCourse : curriculumCourses) {

            /*
             * 必修ではない科目は対象外。
             */
            if (!REQUIREMENT_REQUIRED.equals(curriculumCourse.getRequirementType())) {
                continue;
            }

            Integer courseId = curriculumCourse.getCourseId();

            /*
             * 科目情報を取得する。
             */
            var course = courseRepository.findById(courseId);

            /*
             * 科目が削除されているなど、
             * 不正な参照になっている場合は
             * 表示対象から除外する。
             */
            if (course == null) {
                continue;
            }

            /*
             * 修得済みかどうかを確認する。
             */
            boolean passed = passedCourseIds.contains(courseId);

            statuses.add(new MandatoryCourseStatus(course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), passed));
        }

        return statuses;
    }

    /**
     * 1つの卒業要件を満たしているか確認する。
     */
    private boolean isRequirementSatisfied(GraduationRequirement requirement) throws SQLException {

        double earnedCredits = getEarnedCredits(requirement);

        return earnedCredits >= requirement.getRequiredCredits();
    }

    /**
     * 卒業要件に対する修得単位数を取得する。
     */
    public double getEarnedCredits(GraduationRequirement requirement) throws SQLException {

        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(requirement.getCurriculumId());

        Set<Integer> countedCourseIds = new HashSet<>();

        double earnedCredits = 0.0;

        for (Enrollment enrollment : enrollments) {

            /*
             * 同じ科目を複数回履修していても
             * 単位は1回だけ計算する。
             */
            if (!countedCourseIds.add(enrollment.getCourseId())) {
                continue;
            }

            Integer gradeId = enrollment.getGradeId();

            if (gradeId == null) {
                continue;
            }

            GradeDef grade = gradeDefRepository.findById(gradeId);

            if (grade == null || !grade.isPassed()) {

                continue;
            }

            /*
             * カテゴリ指定がない場合。
             */
            if (requirement.getCategoryId() == null) {

                var course = courseRepository.findById(enrollment.getCourseId());

                if (course != null) {

                    earnedCredits += course.getCredits();
                }

                continue;
            }

            /*
             * カテゴリ指定がある場合。
             */
            earnedCredits += getEarnedCreditsByCategory(enrollment, requirement.getCategoryId());
        }

        return earnedCredits;
    }

    /**
     * 指定カテゴリに属する科目の
     * 修得単位数を取得する。
     */
    private double getEarnedCreditsByCategory(Enrollment enrollment, Integer categoryId) throws SQLException {

        var course = courseRepository.findById(enrollment.getCourseId());

        if (course == null) {
            return 0.0;
        }

        Integer courseCategoryId = course.getCategoryId();

        if (courseCategoryId == null) {
            return 0.0;
        }

        Set<Integer> categoryIds = new HashSet<>();

        collectCategoryIds(categoryId, categoryIds);

        if (!categoryIds.contains(courseCategoryId)) {
            return 0.0;
        }

        return course.getCredits();
    }

    /**
     * 指定カテゴリと子カテゴリを
     * 再帰的に取得する。
     */
    private void collectCategoryIds(Integer categoryId, Set<Integer> categoryIds) throws SQLException {

        if (categoryId == null || categoryIds.contains(categoryId)) {

            return;
        }

        categoryIds.add(categoryId);

        List<CourseCategory> children = categoryRepository.findByParentId(categoryId);

        for (CourseCategory child : children) {

            collectCategoryIds(child.getId(), categoryIds);
        }
    }

    /**
     * 残り必要単位数を取得する。
     */
    public double getRemainingCredits(GraduationRequirement requirement) throws SQLException {

        double earnedCredits = getEarnedCredits(requirement);

        return Math.max(0.0, requirement.getRequiredCredits() - earnedCredits);
    }

    /**
     * カリキュラムの卒業要件一覧を取得する。
     */
    public List<GraduationRequirement> getRequirements(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        return graduationRequirementRepository.findByCurriculumId(curriculumId);
    }

    /**
     * 卒業要件の表示用データを取得する。
     */
    public List<GraduationRequirementDisplay> getRequirementDisplays(Integer curriculumId) throws SQLException {

        validateCurriculumId(curriculumId);

        List<GraduationRequirement> requirements = getRequirements(curriculumId);

        List<GraduationRequirementDisplay> displays = new ArrayList<>();

        for (GraduationRequirement requirement : requirements) {

            double earned = getEarnedCredits(requirement);

            double remaining = Math.max(0.0, requirement.getRequiredCredits() - earned);

            /*
             * 既存の
             * GraduationRequirementDisplay の
             * 3引数コンストラクタを使用する。
             */
            displays.add(new GraduationRequirementDisplay(requirement.getName(), earned, remaining));
        }

        return displays;
    }

    /**
     * カリキュラムIDを検証する。
     */
    private void validateCurriculumId(Integer curriculumId) {

        if (curriculumId == null) {

            throw new IllegalArgumentException("カリキュラムIDが指定されていません。");
        }
    }
}