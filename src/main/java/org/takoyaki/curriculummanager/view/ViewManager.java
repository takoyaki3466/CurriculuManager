package org.takoyaki.curriculummanager.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.takoyaki.curriculummanager.HelloApplication;
import java.io.IOException;

public class ViewManager {
    private final StackPane contentPane;

    public ViewManager(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    public void show(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Node view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("画面を読み込めませんでした: " + fxmlFile, e);
        }
    }

    public void showDashboard() {
        show("dashboard-view.fxml");
    }

    public void showEnrollment() {
        show("enrollment-view.fxml");
    }

    public void showGrade() {
        show("grade-view.fxml");
    }

    public void showCurriculum() {
        show("curriculum-view.fxml");
    }

    public void showGraduation() {
        show("graduation-view.fxml");
    }
}
