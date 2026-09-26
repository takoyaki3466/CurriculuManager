package org.takoyaki.curriculummanager.controller.abstracts;

import javafx.scene.control.Label;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;
import org.takoyaki.curriculummanager.service.abstracts.AcademicContextService;

public abstract class AbstractAcademicContextController extends AbstractController {
    protected final AcademicContextProvider academicContextService;

    protected AbstractAcademicContextController() {
        this(new AcademicContextService());
    }

    protected AbstractAcademicContextController(AcademicContextProvider academicContextService) {
        this.academicContextService = academicContextService;
    }

    protected Major updateAcademicContext(Label label) {
        Department department = academicContextService.getCurrentDepartment();
        Major major = academicContextService.getCurrentMajor();
        label.setText(formatAcademicContext(department, major));
        return major;
    }
}
