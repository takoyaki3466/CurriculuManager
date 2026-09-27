package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.repository.DepartmentRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.sql.SQLException;
import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.requireEntity;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireText;

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
        requireId(id, I18n.text("validation.department.required"));

        return repository.findById(id);
    }

    public void addDepartment(Department department) throws SQLException {
        validateDepartment(department);
        repository.save(department);
    }

    public void updateDepartment(Department department) throws SQLException {
        requireEntity(department, I18n.text("validation.department.updateRequired"));

        if (department.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.department.updateRequired"));
        }

        validateDepartment(department);
        repository.update(department);
    }

    public void deleteDepartment(Integer id) throws SQLException {
        requireId(id, I18n.text("validation.department.deleteRequired"));

        repository.deleteById(id);
    }

    private void validateDepartment(Department department) {
        requireEntity(department, I18n.text("validation.department.required"));
        department.setName(requireText(department.getName(), I18n.text("validation.department.name")));
    }
}
