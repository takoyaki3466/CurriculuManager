package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.util.List;

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
            throw new RuntimeException("科目一覧の取得に失敗しました。", e);
        }
    }

    public Course getCourse(Integer id) {
        requireId(id, "科目が指定されていません。");

        try {
            return courseRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("科目の取得に失敗しました。", e);
        }
    }

    public List<Course> getCoursesByCategoryId(Integer categoryId) {
        requireId(categoryId, "カテゴリーが指定されていません。");

        try {
            return courseRepository.findByCategoryId(categoryId);
        } catch (Exception e) {
            throw new RuntimeException("カテゴリーに所属する科目の取得に失敗しました。", e);
        }
    }

    public void addCourse(Course course) {
        validateCourse(course);

        try {
            courseRepository.save(course);
        } catch (Exception e) {
            throw new RuntimeException("科目の追加に失敗しました。", e);
        }
    }

    public void updateCourse(Course course) {
        requireEntity(course, "更新する科目が指定されていません。");

        if (course.getId() == null) {
            throw new IllegalArgumentException("更新する科目が指定されていません。");
        }

        validateCourse(course);

        try {
            courseRepository.update(course);
        } catch (Exception e) {
            throw new RuntimeException("科目の更新に失敗しました。", e);
        }
    }

    public void deleteCourse(Integer id) {
        requireId(id, "削除する科目が指定されていません。");

        try {
            courseRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("科目の削除に失敗しました。", e);
        }
    }

    private void validateCourse(Course course) {
        requireEntity(course, "科目が指定されていません。");
        course.setName(requireText(course.getName(), "科目名を入力してください。"));

        if (course.getCredits() < 0) {
            throw new IllegalArgumentException("単位数は0以上で入力してください。");
        }
    }
}
