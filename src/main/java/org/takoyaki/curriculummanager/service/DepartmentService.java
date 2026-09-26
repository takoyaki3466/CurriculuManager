package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.repository.DepartmentRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService extends AbstractRepositoryService<Department> {
    public DepartmentService() {
        this(new DepartmentRepository());
    }

    private DepartmentService(DepartmentRepository repository) {
        super(repository);
    }

    public List<Department> getDepartments() throws SQLException {
        return repository.findAll();
    }

    public Department getDepartment(Integer id) throws SQLException {
        requireId(id, "学部が指定されていません。");

        return repository.findById(id);
    }

    public void addDepartment(Department department) throws SQLException {
        validateDepartment(department);
        repository.save(department);
    }

    public void updateDepartment(Department department) throws SQLException {
        requireEntity(department, "更新する学部が指定されていません。");

        if (department.getId() == null) {
            throw new IllegalArgumentException("更新する学部が指定されていません。");
        }

        validateDepartment(department);
        repository.update(department);
    }

    public void deleteDepartment(Integer id) throws SQLException {
        requireId(id, "削除する学部が指定されていません。");

        repository.deleteById(id);
    }

    private void validateDepartment(Department department) {
        requireEntity(department, "学部が指定されていません。");
        department.setName(requireText(department.getName(), "学部名を入力してください。"));
    }
}
