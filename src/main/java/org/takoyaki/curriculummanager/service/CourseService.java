package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.requireEntity;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireText;

public class CourseService extends AbstractRepositoryService<Course> {
    private final CourseRepository courseRepository;

    public CourseService() {
        this(new CourseRepository());
    }

    private CourseService(CourseRepository courseRepository) {
        super(courseRepository);
        this.courseRepository = courseRepository;
    }

    public List<Course> getAllCourses() {
        try {
            return courseRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.list"), e);
        }
    }

    public Course getCourse(Integer id) {
        requireId(id, I18n.text("validation.course.required"));

        try {
            return courseRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.load"), e);
        }
    }

    public List<Course> getCoursesByCategoryId(Integer categoryId) {
        requireId(categoryId, I18n.text("validation.category.labelRequired"));

        try {
            return courseRepository.findByCategoryId(categoryId);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.categoryLoad"), e);
        }
    }

    public void addCourse(Course course) {
        validateCourse(course);

        try {
            courseRepository.save(course);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.add"), e);
        }
    }

    public void updateCourse(Course course) {
        requireEntity(course, I18n.text("validation.course.updateRequired"));

        if (course.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.updateRequired"));
        }

        validateCourse(course);

        try {
            courseRepository.update(course);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.update"), e);
        }
    }

    public void deleteCourse(Integer id) {
        requireId(id, I18n.text("validation.course.deleteRequired"));

        try {
            courseRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.course.delete"), e);
        }
    }

    private void validateCourse(Course course) {
        requireEntity(course, I18n.text("validation.course.required"));
        course.setName(requireText(course.getName(), I18n.text("validation.course.name")));

        if (course.getCredits() < 0) {
            throw new IllegalArgumentException(I18n.text("validation.course.creditsNonNegative"));
        }
    }
}
