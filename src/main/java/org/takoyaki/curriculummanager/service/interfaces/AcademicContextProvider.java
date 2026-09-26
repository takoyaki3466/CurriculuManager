package org.takoyaki.curriculummanager.service.interfaces;

import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;

public interface AcademicContextProvider {
    Department getCurrentDepartment();

    Major getCurrentMajor();

    void setCurrent(Department department, Major major);

    void ensureConfigured();
}
