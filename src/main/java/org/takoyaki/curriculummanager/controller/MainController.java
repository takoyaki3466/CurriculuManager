package org.takoyaki.curriculummanager.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.takoyaki.curriculummanager.view.ViewManager;

public class MainController {
    @FXML
    private StackPane contentPane;
    private ViewManager viewManager;

    @FXML
    private void initialize() {
        viewManager = new ViewManager(contentPane);
        viewManager.showDashboard();
    }

    @FXML
    private void openDashboard() {
        viewManager.showDashboard();
    }

    @FXML
    private void openEnrollment() {
        viewManager.showEnrollment();
    }

    @FXML
    private void openGrade() {
        viewManager.showGrade();
    }

    @FXML
    private void openCurriculum() {
        viewManager.showCurriculum();
    }

    @FXML
    private void openGraduation() {
        viewManager.showGraduation();
    }

    @FXML
    private void openDatabaseTransfer() {
        viewManager.showDatabaseTransfer();
    }

    @FXML
    private void openQa() {
        viewManager.showQa();
    }
}
