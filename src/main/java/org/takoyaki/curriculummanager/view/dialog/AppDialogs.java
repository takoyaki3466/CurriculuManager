package org.takoyaki.curriculummanager.view.dialog;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;

public final class AppDialogs {
    private AppDialogs() {
    }

    public static TextInputDialog textInput(String initialValue, String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog(initialValue == null ? "" : initialValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog;
    }

    public static <T> Dialog<T> create(String title, String header) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog;
    }
}
