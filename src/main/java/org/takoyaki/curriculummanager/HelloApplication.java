package org.takoyaki.curriculummanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.takoyaki.curriculummanager.database.DatabaseInit;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.service.abstracts.AcademicContextService;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseInit.initialize();
        AcademicContextProvider academicContextService = new AcademicContextService();
        academicContextService.ensureConfigured();

        if (academicContextService.getCurrentDepartment() == null || academicContextService.getCurrentMajor() == null) {
            Platform.exit();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
        fxmlLoader.setResources(I18n.bundle());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle(I18n.text("app.window.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
