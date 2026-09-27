package org.takoyaki.curriculummanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractController;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.service.DatabaseTransferService;
import org.takoyaki.curriculummanager.service.abstracts.AcademicContextService;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;
import org.takoyaki.curriculummanager.view.dialog.AppAlerts;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseTransferController extends AbstractController {
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    @FXML
    private Label statusLabel;
    private final DatabaseTransferService transferService;

    public DatabaseTransferController() {
        transferService = new DatabaseTransferService();
    }

    @FXML
    private void exportDatabase() {
        FileChooser chooser = createChooser(I18n.text("databaseTransfer.export.title"));
        chooser.setInitialFileName("curriculum-backup-" + FILE_DATE_FORMAT.format(LocalDateTime.now()) + ".json");
        File selected = chooser.showSaveDialog(statusLabel.getScene().getWindow());

        if (selected == null) {
            return;
        }

        File destination = selected.getName().toLowerCase().endsWith(".json")
                ? selected
                : new File(selected.getAbsolutePath() + ".json");

        try {
            transferService.exportDatabase(destination.toPath());
            statusLabel.setText(I18n.text("databaseTransfer.export.complete", destination.getAbsolutePath()));
            showInformation(I18n.text("databaseTransfer.export.success"));
        } catch (Exception e) {
            showError(I18n.text("databaseTransfer.export.error", e.getMessage()));
        }
    }

    @FXML
    private void importDatabase() {
        FileChooser chooser = createChooser(I18n.text("databaseTransfer.import.title"));
        File source = chooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (source == null) {
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("databaseTransfer.import.confirm.title"),
                I18n.text("databaseTransfer.import.confirm.header"),
                I18n.text("databaseTransfer.import.confirm.content", source.getAbsolutePath())
        );

        if (!confirmed) {
            return;
        }

        try {
            transferService.importDatabase(source.toPath());
            AcademicContextProvider academicContextService = new AcademicContextService();
            academicContextService.ensureConfigured();
            statusLabel.setText(I18n.text("databaseTransfer.import.complete", source.getAbsolutePath()));
            showInformation(I18n.text("databaseTransfer.import.success"));
        } catch (Exception e) {
            showError(I18n.text("databaseTransfer.import.error", e.getMessage()));
        }
    }

    private FileChooser createChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.text("databaseTransfer.jsonFiles"),
                "*.json"
        ));
        return chooser;
    }
}
