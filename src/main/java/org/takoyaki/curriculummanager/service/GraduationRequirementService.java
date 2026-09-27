package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
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

import static org.takoyaki.curriculummanager.util.RequirementTypeUtils.isRequired;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateCurriculumId;

public class GraduationRequirementService {
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

    public boolean isGraduated(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return areRequirementsSatisfied(curriculumId)
                && isMandatoryCoursesSatisfied(curriculumId);
    }

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

    public boolean hasRequirements(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return !graduationRequirementRepository.findByCurriculumId(curriculumId).isEmpty();
    }

    public boolean hasMandatoryCourses(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return curriculumCourseRepository.findByCurriculumId(curriculumId).stream()
                .anyMatch(curriculumCourse -> isRequired(curriculumCourse.getRequirementType()));
    }

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

    private boolean isRequirementSatisfied(GraduationRequirement requirement) throws SQLException {
        double earnedCredits = getEarnedCredits(requirement);
        return earnedCredits >= requirement.getRequiredCredits();
    }

    public double getEarnedCredits(GraduationRequirement requirement) throws SQLException {
        if (requirement == null) {
            throw new IllegalArgumentException(I18n.text("validation.graduationRequirement.required"));
        }

        validateCurriculumId(requirement.getCurriculumId());
        List<Enrollment> enrollments = enrollmentRepository.findByCurriculumId(requirement.getCurriculumId());
        Set<Integer> passedCourseIds = getPassedCourseIds(enrollments);
        Set<Integer> targetCategoryIds = collectTargetCategoryIds(requirement.getCategoryIds());
        boolean allCategories = requirement.isAllCategories();
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

            if (allCategories) {
                earnedCredits += course.getCredits();
                continue;
            }

            Integer courseCategoryId = course.getCategoryId();

            if (courseCategoryId == null) {
                continue;
            }

            if (targetCategoryIds.contains(courseCategoryId)) {
                earnedCredits += course.getCredits();
            }
        }

        return earnedCredits;
    }

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

    private void collectCategoryIds(Integer categoryId, Set<Integer> categoryIds) throws SQLException {
        if (categoryId == null) {
            return;
        }

        if (!categoryIds.add(categoryId)) {
            return;
        }

        List<CourseCategory> children = categoryRepository.findByParentId(categoryId);

        for (CourseCategory child : children) {
            collectCategoryIds(child.getId(), categoryIds);
        }
    }

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

    public double getRemainingCredits(GraduationRequirement requirement) throws SQLException {
        double earnedCredits = getEarnedCredits(requirement);
        return Math.max(0.0, requirement.getRequiredCredits() - earnedCredits);
    }

    public List<GraduationRequirement> getRequirements(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return graduationRequirementRepository.findByCurriculumId(curriculumId);
    }

    public List<GraduationRequirementDisplay> getRequirementDisplays(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        List<GraduationRequirement> requirements = getRequirements(curriculumId);
        List<GraduationRequirementDisplay> displays = new ArrayList<>();

        for (GraduationRequirement requirement : requirements) {
            double earned = getEarnedCredits(requirement);
            String targetCategories = getTargetCategoriesText(requirement);
            displays.add(new GraduationRequirementDisplay(requirement.getId(), requirement.getName(), targetCategories, requirement.getRequiredCredits(), earned));
        }

        return displays;
    }

    private String getTargetCategoriesText(GraduationRequirement requirement) throws SQLException {
        if (requirement.isAllCategories()) {
            return I18n.text("graduation.category.all");
        }

        List<String> categoryNames = new ArrayList<>();

        for (Integer categoryId : requirement.getCategoryIds()) {
            if (categoryId == null) {
                continue;
            }

            CourseCategory category = categoryRepository.findById(categoryId);

            if (category == null) {
                continue;
            }

            categoryNames.add(category.getName());
        }

        if (categoryNames.isEmpty()) {
            return I18n.text("graduation.category.none");
        }

        return String.join(" / ", categoryNames);
    }

}
