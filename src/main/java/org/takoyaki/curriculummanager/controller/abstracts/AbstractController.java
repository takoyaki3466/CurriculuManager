package org.takoyaki.curriculummanager.controller.abstracts;

import javafx.scene.control.Alert;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;

public abstract class AbstractController {
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("エラー");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showError(String message, Throwable exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("エラー");
        alert.setHeaderText(message);

        if (exception != null) {
            alert.setContentText(exception.getMessage());
        }

        alert.showAndWait();
    }

    protected void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("完了");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected String formatAcademicContext(Department department, Major major) {
        if (department == null || major == null) {
            return "学部・学科未設定";
        }

        return department.getName() + "　/　" + major.getName();
    }
}
