package org.takoyaki.curriculummanager.util;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import org.takoyaki.curriculummanager.model.Terminology;

public final class TerminologyUtils {
    private static final String CURRICULUM_TOKEN = "\u0001CURRICULUM\u0001";

    private TerminologyUtils() {
    }

    public static String format(String text, Terminology terminology) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.replace(Terminology.DEFAULT_CURRICULUM_NAME, CURRICULUM_TOKEN)
                .replace(CURRICULUM_TOKEN, terminology.curriculumName());
    }

    public static void apply(Node root, Terminology terminology) {
        if (root == null) {
            return;
        }

        if (root instanceof Labeled labeled) {
            labeled.setText(format(labeled.getText(), terminology));
        }

        if (root instanceof TextInputControl inputControl) {
            inputControl.setPromptText(format(inputControl.getPromptText(), terminology));
        }

        if (root instanceof ComboBox<?> comboBox) {
            comboBox.setPromptText(format(comboBox.getPromptText(), terminology));
        }

        if (root instanceof TableView<?> tableView) {
            tableView.getColumns().forEach(column -> applyColumn(column, terminology));
            apply(tableView.getPlaceholder(), terminology);
        }

        if (root instanceof ListView<?> listView) {
            apply(listView.getPlaceholder(), terminology);
        }

        if (root instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> apply(child, terminology));
        }
    }

    public static void installWindowLocalization(Terminology terminology) {
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                for (Window window : change.getAddedSubList()) {
                    window.showingProperty().addListener((observable, oldValue, showing) -> {
                        if (showing && window.getScene() != null) {
                            Platform.runLater(() -> apply(window.getScene().getRoot(), terminology));
                        }
                    });
                }
            }
        });
    }

    private static void applyColumn(TableColumn<?, ?> column, Terminology terminology) {
        column.setText(format(column.getText(), terminology));
        column.getColumns().forEach(child -> applyColumn(child, terminology));
    }
}
