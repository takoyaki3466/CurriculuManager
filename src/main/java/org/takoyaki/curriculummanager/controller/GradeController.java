package org.takoyaki.curriculummanager.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractAcademicContextController;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.GradeService;
import java.sql.SQLException;
import java.util.List;

public class GradeController extends AbstractAcademicContextController {
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private javafx.scene.control.Label academicContextLabel;
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;
    @FXML
    private TableView<EnrollmentDisplay> gradeTable;
    @FXML
    private TableColumn<EnrollmentDisplay, Number> yearColumn;
    @FXML
    private TableColumn<EnrollmentDisplay, String> semesterColumn;
    @FXML
    private TableColumn<EnrollmentDisplay, String> courseCodeColumn;
    @FXML
    private TableColumn<EnrollmentDisplay, String> courseNameColumn;
    @FXML
    private TableColumn<EnrollmentDisplay, Number> creditsColumn;
    @FXML
    private TableColumn<EnrollmentDisplay, String> gradeColumn;
    private final CurriculumService curriculumService;
    private final GradeService gradeService;
    private List<GradeDef> gradeDefs;

    public GradeController() {
        curriculumService = new CurriculumService();
        gradeService = new GradeService();
    }

    @FXML
    private void initialize() throws SQLException {
        loadGradeDefs();
        setupColumns();
        setupCurriculumComboBox();
        loadAcademicContext();
    }

    private void loadAcademicContext() throws SQLException {
        org.takoyaki.curriculummanager.model.Major major = updateAcademicContext(academicContextLabel);
        loadCurricula(major);
    }

    private void setupColumns() {
        yearColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getYear()));
        semesterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester()));
        courseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        courseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCredits()));
        setupGradeColumn();
    }

    private void loadGradeDefs() throws SQLException {
        gradeDefs = gradeService.getGradeDefs();
    }

    private void setupGradeColumn() {
        gradeTable.setEditable(true);
        gradeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(getGradeSymbols())));
        gradeColumn.setOnEditCommit(event -> {
            EnrollmentDisplay display = event.getRowValue();
            String newSymbol = event.getNewValue();

            if (display == null) {
                return;
            }

            try {
                updateGrade(display, newSymbol);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<String> getGradeSymbols() {
        return gradeDefs.stream().map(GradeDef::getSymbol).toList();
    }

    private void setupCurriculumComboBox() {
        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadEnrollments(newValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadCurricula(org.takoyaki.curriculummanager.model.Major major) throws SQLException {
        curriculumComboBox.getItems().clear();
        gradeTable.getItems().clear();

        if (major == null || major.getId() == null) {
            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(major.getId());
        var items = FXCollections.<Curriculum>observableArrayList();
        items.add(Curriculum.allOption());
        items.addAll(curricula);
        curriculumComboBox.setItems(items);

        if (!items.isEmpty()) {
            curriculumComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadEnrollments(Curriculum curriculum) throws SQLException {
        gradeTable.getItems().clear();

        if (curriculum == null || curriculum.getId() == null) {
            gradeTable.setEditable(false);
            return;
        }

        if (curriculum.isAllOption()) {
            List<EnrollmentDisplay> displays = new java.util.ArrayList<>();

            for (Curriculum item : curriculumComboBox.getItems()) {
                if (item != null && !item.isAllOption()) {
                    displays.addAll(gradeService.getEnrollmentDisplaysByCurriculum(item.getId()));
                }
            }

            gradeTable.setItems(FXCollections.observableArrayList(displays));
            gradeTable.setEditable(false);
            return;
        }

        gradeTable.setEditable(true);
        List<EnrollmentDisplay> displays = gradeService.getEnrollmentDisplaysByCurriculum(curriculum.getId());
        gradeTable.setItems(FXCollections.observableArrayList(displays));
    }

    private void updateGrade(EnrollmentDisplay display, String newSymbol) throws SQLException {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null || curriculum.isAllOption()) {
            showError("カリキュラムが選択されていません。");
            refresh();
            return;
        }

        if (newSymbol == null || newSymbol.isBlank()) {
            showError("成績が選択されていません。");
            refresh();
            return;
        }

        GradeDef selectedGrade = gradeDefs.stream().filter(grade -> newSymbol.equals(grade.getSymbol())).findFirst().orElse(null);

        if (selectedGrade == null) {
            showError("指定された成績が存在しません: " + newSymbol);
            refresh();
            return;
        }

        try {
            gradeService.updateGradeByCurriculum(curriculum.getId(), display.getEnrollmentId(), selectedGrade.getId());
            loadEnrollments(curriculum);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            refresh();
        }
    }

    @FXML
    private void refresh() throws SQLException {
        loadEnrollments(curriculumComboBox.getValue());
    }

}
