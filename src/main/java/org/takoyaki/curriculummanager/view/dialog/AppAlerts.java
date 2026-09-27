package org.takoyaki.curriculummanager.view.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.takoyaki.curriculummanager.i18n.I18n;

public final class AppAlerts {
    private AppAlerts() {
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, I18n.text("dialog.error.title"), null, message);
    }

    public static void error(String header, Throwable exception) {
        show(
                Alert.AlertType.ERROR,
                I18n.text("dialog.error.title"),
                header,
                exception == null ? null : exception.getMessage()
        );
    }

    public static void information(String message) {
        show(Alert.AlertType.INFORMATION, I18n.text("dialog.complete.title"), null, message);
    }

    public static boolean confirm(String title, String header, String content) {
        Alert alert = create(Alert.AlertType.CONFIRMATION, title, header, content);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static void show(Alert.AlertType type, String title, String header, String content) {
        create(type, title, header, content).showAndWait();
    }

    private static Alert create(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
}
