package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.CurriculumCourseDisplay;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.CurriculumCourseRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.requireText;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateCourseId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.validateCurriculumId;

public class CurriculumCourseService {
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final CourseRepository courseRepository;

    public CurriculumCourseService() {
        curriculumCourseRepository = new CurriculumCourseRepository();
        courseRepository = new CourseRepository();
    }

    public List<CurriculumCourse> getCurriculumCourses(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return curriculumCourseRepository.findByCurriculumId(curriculumId);
    }

    public List<Integer> getCourseIds(Integer curriculumId) throws SQLException {
        return getCurriculumCourses(curriculumId).stream().map(CurriculumCourse::getCourseId).filter(id -> id != null).toList();
    }

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

    public List<CurriculumCourseDisplay> getCourseDisplaysByCategory(Integer curriculumId, Integer categoryId) throws SQLException {
        validateCurriculumId(curriculumId);

        if (categoryId == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.idRequired"));
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.category.idInvalid"));
        }

        List<Course> courses = courseRepository.findByCategoryId(categoryId);
        List<CurriculumCourse> curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);
        List<CurriculumCourseDisplay> displays = new ArrayList<>();

        for (Course course : courses) {
            if (course == null) {
                continue;
            }

            CurriculumCourse relation = findRelation(curriculumCourses, course.getId());
            String requirementType = null;
            Integer relationId = null;

            if (relation != null) {
                relationId = relation.getId();
                requirementType = relation.getRequirementType();
            }

            displays.add(new CurriculumCourseDisplay(relationId, curriculumId, course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), course.getCategoryId(), requirementType));
        }

        return displays;
    }

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

    public boolean exists(Integer curriculumId, Integer courseId) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);
        return curriculumCourseRepository.findByCurriculumIdAndCourseId(curriculumId, courseId) != null;
    }

    public void addCourseToCurriculum(Integer curriculumId, Integer courseId, String requirementType) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);
        requireText(requirementType, I18n.text("validation.requirementType.required"));
        Course course = courseRepository.findById(courseId);

        if (course == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.missing", courseId));
        }

        if (exists(curriculumId, courseId)) {
            throw new IllegalArgumentException(I18n.text("validation.course.alreadyRegistered"));
        }

        CurriculumCourse curriculumCourse = new CurriculumCourse(curriculumId, courseId, requirementType);
        curriculumCourseRepository.save(curriculumCourse);
    }

    public void update(CurriculumCourse curriculumCourse) throws SQLException {
        if (curriculumCourse == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculumCourse.null"));
        }

        if (curriculumCourse.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.update.idRequired"));
        }

        validateCurriculumId(curriculumCourse.getCurriculumId());
        validateCourseId(curriculumCourse.getCourseId());
        requireText(curriculumCourse.getRequirementType(), I18n.text("validation.requirementType.required"));
        curriculumCourseRepository.update(curriculumCourse);
    }

    public void removeCourseFromCurriculum(Integer curriculumId, Integer courseId) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);

        if (!exists(curriculumId, courseId)) {
            throw new IllegalArgumentException(I18n.text("validation.course.notRegistered"));
        }

        curriculumCourseRepository.deleteByCurriculumIdAndCourseId(curriculumId, courseId);
    }

    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.delete.idRequired"));
        }

        if (curriculumCourseRepository.findById(id) == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculumCourse.missing", id));
        }

        curriculumCourseRepository.deleteById(id);
    }

}
