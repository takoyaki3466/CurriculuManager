package org.takoyaki.curriculummanager.service.interfaces;

import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Major;

public interface AcademicContextProvider {
    Department getCurrentDepartment();

    Major getCurrentMajor();

    Curriculum getCurrentCurriculum();

    void setCurrent(Department department, Major major);

    void setCurrentCurriculum(Curriculum curriculum);

    void ensureConfigured();
}
