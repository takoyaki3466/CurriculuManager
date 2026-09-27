package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
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
            throw new IllegalArgumentException(I18n.text("validation.department.required"));
        }

        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.department.name"));
        }

        departmentRepository.save(department);
    }

    public void updateDepartment(Department department) throws SQLException {
        if (department == null || department.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.department.updateRequired"));
        }

        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.department.name"));
        }

        departmentRepository.update(department);
    }

    public void deleteDepartment(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.department.deleteRequired"));
        }

        departmentRepository.deleteById(id);
    }

    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    public List<Major> getMajors(Integer departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException(I18n.text("validation.department.required"));
        }

        return majorRepository.findByDepartmentId(departmentId);
    }

    public Major getMajor(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.required"));
        }

        return majorRepository.findById(id);
    }

    public void addMajor(Major major) {
        if (major == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.required"));
        }

        if (major.getDepartmentId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.departmentRequired"));
        }

        if (major.getName() == null || major.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.major.name"));
        }

        majorRepository.save(major);
    }

    public void updateMajor(Major major) {
        if (major == null || major.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.updateRequired"));
        }

        if (major.getDepartmentId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.departmentRequired"));
        }

        if (major.getName() == null || major.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.major.name"));
        }

        majorRepository.update(major);
    }

    public void deleteMajor(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.deleteRequired"));
        }

        majorRepository.deleteById(id);
    }

    public List<Curriculum> getCurricula(Integer majorId) {
        if (majorId == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.required"));
        }

        return curriculumRepository.findByMajorId(majorId);
    }

    public List<Curriculum> getAllCurricula() {
        return curriculumRepository.findAll();
    }

    public Curriculum getCurriculum(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.required"));
        }

        return curriculumRepository.findById(id);
    }

    public void addCurriculum(Curriculum curriculum) {
        if (curriculum == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.required"));
        }

        if (curriculum.getMajorId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.required"));
        }

        if (curriculum.getName() == null || curriculum.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.name"));
        }

        if (curriculum.getStartYear() <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.year"));
        }

        curriculumRepository.save(curriculum);
    }

    public void updateCurriculum(Curriculum curriculum) {
        if (curriculum == null || curriculum.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.updateRequired"));
        }

        if (curriculum.getMajorId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.major.required"));
        }

        if (curriculum.getName() == null || curriculum.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.name"));
        }

        if (curriculum.getStartYear() <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.year"));
        }

        curriculumRepository.update(curriculum);
    }

    public void deleteCurriculum(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.deleteRequired"));
        }

        curriculumRepository.deleteById(id);
    }

    public List<CourseCategory> getCategories(Integer curriculumId) throws SQLException {
        if (curriculumId == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.required"));
        }

        return categoryRepository.findByCurriculumId(curriculumId);
    }

    public void addCategory(CourseCategory category) throws SQLException {
        if (category == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.required"));
        }

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.curriculumRequired"));
        }

        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.category.name"));
        }

        categoryRepository.save(category);
    }

    public List<Course> getCourses(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.required"));
        }

        return courseRepository.findByCategoryId(categoryId);
    }

    public Course getCourse(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.required"));
        }

        return courseRepository.findById(id);
    }

    public void addCourse(Course course) throws SQLException {
        if (course == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.required"));
        }

        if (course.getName() == null || course.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.course.name"));
        }

        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.course.creditsPositive"));
        }

        courseRepository.save(course);
    }

    public void updateCourse(Course course) throws SQLException {
        if (course == null || course.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.updateRequired"));
        }

        if (course.getName() == null || course.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.course.name"));
        }

        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.course.creditsPositive"));
        }

        courseRepository.update(course);
    }

    public void deleteCourse(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException(I18n.text("validation.course.deleteRequired"));
        }

        courseRepository.deleteById(id);
    }

    public void updateCategory(CourseCategory category) {
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.updateRequired"));
        }

        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.category.name"));
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
            throw new IllegalArgumentException(I18n.text("validation.category.deleteRequired"));
        }

        try {
            categoryRepository.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
