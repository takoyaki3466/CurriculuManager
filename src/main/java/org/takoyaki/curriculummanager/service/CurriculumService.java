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

public class CurriculumService {
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final CurriculumRepository curriculumRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public CurriculumService() {
        departmentRepository = new DepartmentRepository();
        majorRepository = new MajorRepository();
        curriculumRepository = new CurriculumRepository();
        categoryRepository = new CourseCategoryRepository();
        courseRepository = new CourseRepository();
    }

    public List<Department> getDepartments() throws SQLException {
        return departmentRepository.findAll();
    }

    public void addDepartment(Department department) throws SQLException {
        if (department == null) {
            throw new IllegalArgumentException("学部が指定されていません。");
        }

        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException("学部名を入力してください。");
        }

        departmentRepository.save(department);
    }

    public void updateDepartment(Department department) throws SQLException {
        if (department == null || department.getId() == null) {
            throw new IllegalArgumentException("更新する学部が指定されていません。");
        }

        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException("学部名を入力してください。");
        }

        departmentRepository.update(department);
    }

    public void deleteDepartment(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("削除する学部が指定されていません。");
        }

        departmentRepository.deleteById(id);
    }

    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    public List<Major> getMajors(Integer departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("学部が指定されていません。");
        }

        return majorRepository.findByDepartmentId(departmentId);
    }

    public Major getMajor(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("学科が指定されていません。");
        }

        return majorRepository.findById(id);
    }

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

    public void deleteMajor(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("削除する学科が指定されていません。");
        }

        majorRepository.deleteById(id);
    }

    public List<Curriculum> getCurricula(Integer majorId) {
        if (majorId == null) {
            throw new IllegalArgumentException("学科が指定されていません。");
        }

        return curriculumRepository.findByMajorId(majorId);
    }

    public List<Curriculum> getAllCurricula() {
        return curriculumRepository.findAll();
    }

    public Curriculum getCurriculum(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        return curriculumRepository.findById(id);
    }

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

    public void deleteCurriculum(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("削除するカリキュラムが指定されていません。");
        }

        curriculumRepository.deleteById(id);
    }

    public List<CourseCategory> getCategories(Integer curriculumId) throws SQLException {
        if (curriculumId == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        return categoryRepository.findByCurriculumId(curriculumId);
    }

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

    public List<Course> getCourses(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("カテゴリが指定されていません。");
        }

        return courseRepository.findByCategoryId(categoryId);
    }

    public Course getCourse(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("科目が指定されていません。");
        }

        return courseRepository.findById(id);
    }

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

    public void deleteCourse(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("削除する科目が指定されていません。");
        }

        courseRepository.deleteById(id);
    }

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
