package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.repository.CourseRepository;

import java.util.List;

/**
 * 科目に関する処理を担当するServiceです。
 *
 * <p>
 * ControllerからRepositoryを直接操作せず、
 * 科目に関する業務処理をこのクラスにまとめます。
 * </p>
 */
public class CourseService {

    /**
     * 科目を操作するRepositoryです。
     */
    private final CourseRepository courseRepository;

    /**
     * Serviceを生成します。
     */
    public CourseService() {
        this.courseRepository = new CourseRepository();
    }

    /**
     * すべての科目を取得します。
     *
     * @return 科目一覧
     */
    public List<Course> getAllCourses() {

        try {

            return courseRepository.findAll();

        } catch (Exception e) {

            throw new RuntimeException("科目一覧の取得に失敗しました。", e);
        }
    }

    /**
     * IDを指定して科目を取得します。
     *
     * @param id 科目ID
     * @return 科目
     */
    public Course getCourse(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("科目が指定されていません。");
        }

        try {

            return courseRepository.findById(id);

        } catch (Exception e) {

            throw new RuntimeException("科目の取得に失敗しました。", e);
        }
    }

    /**
     * 指定したカテゴリーに所属する科目を取得します。
     *
     * @param categoryId カテゴリーID
     * @return 科目一覧
     */
    public List<Course> getCoursesByCategoryId(Integer categoryId) {

        if (categoryId == null) {
            throw new IllegalArgumentException("カテゴリーが指定されていません。");
        }

        try {

            return courseRepository.findByCategoryId(categoryId);

        } catch (Exception e) {

            throw new RuntimeException("カテゴリーに所属する科目の取得に失敗しました。", e);
        }
    }

    /**
     * 科目を新規登録します。
     *
     * @param course 登録する科目
     */
    public void addCourse(Course course) {

        validateCourse(course);

        try {

            courseRepository.save(course);

        } catch (Exception e) {

            throw new RuntimeException("科目の追加に失敗しました。", e);
        }
    }

    /**
     * 科目を更新します。
     *
     * @param course 更新する科目
     */
    public void updateCourse(Course course) {

        if (course == null || course.getId() == null) {

            throw new IllegalArgumentException("更新する科目が指定されていません。");
        }

        validateCourse(course);

        try {

            courseRepository.update(course);

        } catch (Exception e) {

            throw new RuntimeException("科目の更新に失敗しました。", e);
        }
    }

    /**
     * 科目を削除します。
     *
     * <p>
     * 科目を削除すると、データベース側の
     * ON DELETE CASCADE により、
     * カリキュラムと科目の関連情報や履修情報も
     * 削除されます。
     * </p>
     *
     * @param id 削除する科目ID
     */
    public void deleteCourse(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("削除する科目が指定されていません。");
        }

        try {

            courseRepository.deleteById(id);

        } catch (Exception e) {

            throw new RuntimeException("科目の削除に失敗しました。", e);
        }
    }

    /**
     * 科目の入力内容を検証します。
     *
     * @param course 検証する科目
     */
    private void validateCourse(Course course) {

        if (course == null) {

            throw new IllegalArgumentException("科目が指定されていません。");
        }

        if (course.getName() == null || course.getName().isBlank()) {

            throw new IllegalArgumentException("科目名を入力してください。");
        }

        if (course.getCredits() < 0) {

            throw new IllegalArgumentException("単位数は0以上で入力してください。");
        }
    }
}
