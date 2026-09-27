package org.takoyaki.curriculummanager.controller.abstracts;

import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import org.takoyaki.curriculummanager.model.Curriculum;
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

    protected void saveCurrentCurriculum(Curriculum curriculum) {
        if (curriculum != null) {
            academicContextService.setCurrentCurriculum(curriculum);
        }
    }

    protected void selectCurrentCurriculum(ComboBox<Curriculum> comboBox) {
        Curriculum current = academicContextService.getCurrentCurriculum();

        if (current != null) {
            for (Curriculum curriculum : comboBox.getItems()) {
                if (current.getId().equals(curriculum.getId())) {
                    comboBox.getSelectionModel().select(curriculum);
                    return;
                }
            }
        }

        if (!comboBox.getItems().isEmpty()) {
            comboBox.getSelectionModel().selectFirst();
        }
    }
}
