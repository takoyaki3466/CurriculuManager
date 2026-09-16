package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.repository.CourseCategoryRepository;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.CurriculumRepository;
import org.takoyaki.curriculummanager.repository.DepartmentRepository;
import org.takoyaki.curriculummanager.repository.MajorRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * カリキュラムに関する処理をまとめるService。
 *
 * <p>
 * 以下の階層構造を管理します。
 * </p>
 *
 * <pre>
 * 学部
 *  ↓
 * 学科
 *  ↓
 * カリキュラム
 *  ↓
 * 科目カテゴリ
 *  ↓
 * 科目
 * </pre>
 *
 * <p>
 * ControllerからRepositoryを直接操作させず、
 * データ取得・登録などの処理をこのServiceに集約します。
 * </p>
 */
public class CurriculumService {

    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final CurriculumRepository curriculumRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    /**
     * Serviceを生成します。
     */
    public CurriculumService() {

        departmentRepository = new DepartmentRepository();

        majorRepository = new MajorRepository();

        curriculumRepository = new CurriculumRepository();

        categoryRepository = new CourseCategoryRepository();

        courseRepository = new CourseRepository();
    }

    // =========================================================
    // 学部
    // =========================================================

    /**
     * 学部一覧を取得します。
     *
     * @return 学部一覧
     */
    public List<Department> getDepartments() throws SQLException {

        return departmentRepository.findAll();
    }

    /**
     * 学部を追加します。
     *
     * @param department 追加する学部
     */
    public void addDepartment(Department department) throws SQLException {

        if (department == null) {

            throw new IllegalArgumentException("学部が指定されていません。");
        }

        if (department.getName() == null || department.getName().isBlank()) {

            throw new IllegalArgumentException("学部名を入力してください。");
        }

        departmentRepository.save(department);
    }

    /**
     * 学部を更新します。
     *
     * @param department 更新する学部
     */
    public void updateDepartment(Department department) throws SQLException {

        if (department == null || department.getId() == null) {

            throw new IllegalArgumentException("更新する学部が指定されていません。");
        }

        if (department.getName() == null || department.getName().isBlank()) {

            throw new IllegalArgumentException("学部名を入力してください。");
        }

        departmentRepository.update(department);
    }

    /**
     * 学部を削除します。
     *
     * @param id 削除する学部のID
     */
    public void deleteDepartment(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("削除する学部が指定されていません。");
        }

        departmentRepository.deleteById(id);
    }

    // =========================================================
    // 学科
    // =========================================================

    /**
     * すべての学科を取得します。
     *
     * @return 学科一覧
     */
    public List<Major> getAllMajors() {

        return majorRepository.findAll();
    }

    /**
     * 指定された学部に所属する学科を取得します。
     *
     * @param departmentId 学部ID
     * @return 学科一覧
     */
    public List<Major> getMajors(Integer departmentId) {

        if (departmentId == null) {

            throw new IllegalArgumentException("学部が指定されていません。");
        }

        return majorRepository.findByDepartmentId(departmentId);
    }

    /**
     * 学科をIDから取得します。
     *
     * @param id 学科ID
     * @return 学科
     */
    public Major getMajor(Integer id) {

        if (id == null) {

            throw new IllegalArgumentException("学科が指定されていません。");
        }

        return majorRepository.findById(id);
    }

    /**
     * 学科を追加します。
     *
     * @param major 追加する学科
     */
    public void addMajor(Major major) {

        if (major == null) {

            throw new IllegalArgumentException("学科が指定されていません。");
        }

        if (major.getDepartmentId() == null) {

            throw new IllegalArgumentException("所属する学部が指定されていません。");
        }

        if (major.getName() == null || major.getName().isBlank()) {

            throw new IllegalArgumentException("学科名を入力してください。");
        }

        majorRepository.save(major);
    }

    /**
     * 学科を更新します。
     *
     * @param major 更新する学科
     */
    public void updateMajor(Major major) {

        if (major == null || major.getId() == null) {

            throw new IllegalArgumentException("更新する学科が指定されていません。");
        }

        if (major.getDepartmentId() == null) {

            throw new IllegalArgumentException("所属する学部が指定されていません。");
        }

        if (major.getName() == null || major.getName().isBlank()) {

            throw new IllegalArgumentException("学科名を入力してください。");
        }

        majorRepository.update(major);
    }

    /**
     * 学科を削除します。
     *
     * @param id 削除する学科のID
     */
    public void deleteMajor(Integer id) {

        if (id == null) {

            throw new IllegalArgumentException("削除する学科が指定されていません。");
        }

        majorRepository.deleteById(id);
    }

    // =========================================================
    // カリキュラム
    // =========================================================

    /**
     * 指定された学科に所属するカリキュラムを取得します。
     *
     * @param majorId 学科ID
     * @return カリキュラム一覧
     */
    public List<Curriculum> getCurricula(Integer majorId) {

        if (majorId == null) {

            throw new IllegalArgumentException("学科が指定されていません。");
        }

        return curriculumRepository.findByMajorId(majorId);
    }

    /**
     * すべてのカリキュラムを取得します。
     *
     * @return カリキュラム一覧
     */
    public List<Curriculum> getAllCurricula() {

        return curriculumRepository.findAll();
    }

    /**
     * カリキュラムをIDから取得します。
     *
     * @param id カリキュラムID
     * @return カリキュラム
     */
    public Curriculum getCurriculum(Integer id) {

        if (id == null) {

            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        return curriculumRepository.findById(id);
    }

    /**
     * カリキュラムを追加します。
     *
     * @param curriculum 追加するカリキュラム
     */
    public void addCurriculum(Curriculum curriculum) {

        if (curriculum == null) {

            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        if (curriculum.getMajorId() == null) {

            throw new IllegalArgumentException("学科が指定されていません。");
        }

        if (curriculum.getName() == null || curriculum.getName().isBlank()) {

            throw new IllegalArgumentException("カリキュラム名を入力してください。");
        }

        if (curriculum.getStartYear() <= 0) {

            throw new IllegalArgumentException("開始年度が正しくありません。");
        }

        curriculumRepository.save(curriculum);
    }

    /**
     * カリキュラムを更新します。
     *
     * @param curriculum 更新するカリキュラム
     */
    public void updateCurriculum(Curriculum curriculum) {

        if (curriculum == null || curriculum.getId() == null) {

            throw new IllegalArgumentException("更新するカリキュラムが指定されていません。");
        }

        if (curriculum.getMajorId() == null) {

            throw new IllegalArgumentException("学科が指定されていません。");
        }

        if (curriculum.getName() == null || curriculum.getName().isBlank()) {

            throw new IllegalArgumentException("カリキュラム名を入力してください。");
        }

        if (curriculum.getStartYear() <= 0) {

            throw new IllegalArgumentException("開始年度が正しくありません。");
        }

        curriculumRepository.update(curriculum);
    }

    /**
     * カリキュラムを削除します。
     *
     * @param id 削除するカリキュラムのID
     */
    public void deleteCurriculum(Integer id) {

        if (id == null) {

            throw new IllegalArgumentException("削除するカリキュラムが指定されていません。");
        }

        curriculumRepository.deleteById(id);
    }

    // =========================================================
    // カテゴリ
    // =========================================================

    /**
     * 指定されたカリキュラムのカテゴリを取得します。
     *
     * @param curriculumId カリキュラムID
     * @return カテゴリ一覧
     */
    public List<CourseCategory> getCategories(Integer curriculumId) throws SQLException {

        if (curriculumId == null) {

            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        return categoryRepository.findByCurriculumId(curriculumId);
    }

    /**
     * カテゴリを追加します。
     *
     * @param category 追加するカテゴリ
     */
    public void addCategory(CourseCategory category) throws SQLException {

        if (category == null) {

            throw new IllegalArgumentException("カテゴリが指定されていません。");
        }

        if (category.getCurriculumId() == null) {

            throw new IllegalArgumentException("所属するカリキュラムが指定されていません。");
        }

        if (category.getName() == null || category.getName().isBlank()) {

            throw new IllegalArgumentException("カテゴリ名を入力してください。");
        }

        categoryRepository.save(category);
    }

    // =========================================================
    // 科目
    // =========================================================

    /**
     * 指定されたカテゴリに所属する科目を取得します。
     *
     * @param categoryId カテゴリID
     * @return 科目一覧
     */
    public List<Course> getCourses(Integer categoryId) throws SQLException {

        if (categoryId == null) {

            throw new IllegalArgumentException("カテゴリが指定されていません。");
        }

        return courseRepository.findByCategoryId(categoryId);
    }

    /**
     * 科目をIDから取得します。
     *
     * @param id 科目ID
     * @return 科目
     */
    public Course getCourse(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("科目が指定されていません。");
        }

        return courseRepository.findById(id);
    }

    /**
     * 科目を追加します。
     *
     * @param course 追加する科目
     */
    public void addCourse(Course course) throws SQLException {

        if (course == null) {

            throw new IllegalArgumentException("科目が指定されていません。");
        }

        if (course.getName() == null || course.getName().isBlank()) {

            throw new IllegalArgumentException("科目名を入力してください。");
        }

        if (course.getCredits() <= 0) {

            throw new IllegalArgumentException("単位数は0より大きい値を指定してください。");
        }

        courseRepository.save(course);
    }

    /**
     * 科目を更新します。
     *
     * @param course 更新する科目
     */
    public void updateCourse(Course course) throws SQLException {

        if (course == null || course.getId() == null) {

            throw new IllegalArgumentException("更新する科目が指定されていません。");
        }

        if (course.getName() == null || course.getName().isBlank()) {

            throw new IllegalArgumentException("科目名を入力してください。");
        }

        if (course.getCredits() <= 0) {

            throw new IllegalArgumentException("単位数は0より大きい値を指定してください。");
        }

        courseRepository.update(course);
    }

    /**
     * 科目を削除します。
     *
     * @param id 削除する科目のID
     */
    public void deleteCourse(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("削除する科目が指定されていません。");
        }

        courseRepository.deleteById(id);
    }

    /**
     * 科目カテゴリを更新します。
     *
     * @param category 更新するカテゴリ
     */
    public void updateCategory(CourseCategory category) {

        if (category == null || category.getId() == null) {


            throw new IllegalArgumentException("更新するカテゴリが指定されていません。");


        }

        if (category.getName() == null || category.getName().isBlank()) {


            throw new IllegalArgumentException("カテゴリ名を入力してください。");


        }

        category.setName(category.getName().trim());

        try {
            categoryRepository.update(category);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 科目カテゴリを削除します。
     *
     * @param id 削除するカテゴリID
     */
    public void deleteCategory(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("削除するカテゴリが指定されていません。");


        }

        try {
            categoryRepository.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
