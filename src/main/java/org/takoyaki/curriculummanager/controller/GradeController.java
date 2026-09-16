package org.takoyaki.curriculummanager.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;

import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.GradeService;

import java.sql.SQLException;
import java.util.List;

/**
 * 成績画面を管理するController。
 *
 * <p>
 * 学部 → カリキュラムを選択し、
 * 選択されたカリキュラムに属する履修だけを
 * 成績一覧へ表示する。
 * </p>
 */
public class GradeController {

    @FXML
    private ComboBox<Department> departmentComboBox;

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

    /**
     * 成績候補。
     */
    private List<GradeDef> gradeDefs;

    public GradeController() {
        curriculumService = new CurriculumService();
        gradeService = new GradeService();
    }

    /**
     * 初期化。
     */
    @FXML
    private void initialize() throws SQLException {

        /*
         * 成績定義を最初に読み込む。
         */
        loadGradeDefs();

        /*
         * TableViewの列を設定する。
         */
        setupColumns();

        /*
         * 学部・カリキュラムの選択変更を監視する。
         */
        setupDepartmentComboBox();
        setupCurriculumComboBox();

        /*
         * 学部一覧を読み込む。
         */
        loadDepartments();
    }

    /**
     * TableViewの列を設定する。
     */
    private void setupColumns() {
        yearColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getYear()));
        semesterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester()));
        courseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        courseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCredits()));

        /*
         * 成績列はComboBoxで編集できるようにする。
         */
        setupGradeColumn();
    }

    /**
     * 成績定義を読み込む。
     */
    private void loadGradeDefs() throws SQLException {

        gradeDefs = gradeService.getGradeDefs();
    }

    /**
     * 成績列を設定する。
     */
    private void setupGradeColumn() {

        gradeTable.setEditable(true);

        /*
         * GradeDefそのものではなく、
         * StringをTableColumnへ設定する。
         *
         * そのためComboBoxTableCellにもStringを渡す。
         */
        gradeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(getGradeSymbols())));

        /*
         * 成績が変更されたときの処理。
         */
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

    /**
     * 成績定義から表示用の記号一覧を作成する。
     */
    private List<String> getGradeSymbols() {

        return gradeDefs.stream().map(GradeDef::getSymbol).toList();
    }

    /**
     * 学部ComboBoxの変更を監視する。
     */
    private void setupDepartmentComboBox() {

        departmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadCurricula(newValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * カリキュラムComboBoxの変更を監視する。
     */
    private void setupCurriculumComboBox() {

        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadEnrollments(newValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 学部一覧を読み込む。
     */
    private void loadDepartments() throws SQLException {

        List<Department> departments = curriculumService.getDepartments();

        departmentComboBox.setItems(FXCollections.observableArrayList(departments));

        if (!departments.isEmpty()) {

            departmentComboBox.getSelectionModel().selectFirst();

        } else {

            curriculumComboBox.getItems().clear();
            gradeTable.getItems().clear();
        }
    }

    /**
     * 選択された学部のカリキュラムを読み込む。
     */
    private void loadCurricula(Department department) throws SQLException {

        curriculumComboBox.getItems().clear();
        gradeTable.getItems().clear();

        if (department == null || department.getId() == null) {

            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(department.getId());

        curriculumComboBox.setItems(FXCollections.observableArrayList(curricula));

        if (!curricula.isEmpty()) {

            curriculumComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * 選択されたカリキュラムの履修を読み込む。
     */
    private void loadEnrollments(Curriculum curriculum) throws SQLException {

        gradeTable.getItems().clear();

        if (curriculum == null || curriculum.getId() == null) {

            return;
        }

        List<EnrollmentDisplay> displays = gradeService.getEnrollmentDisplaysByCurriculum(curriculum.getId());

        gradeTable.setItems(FXCollections.observableArrayList(displays));
    }

    /**
     * 成績を更新する。
     */
    private void updateGrade(EnrollmentDisplay display, String newSymbol) throws SQLException {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {

            showError("カリキュラムが選択されていません。");

            refresh();

            return;
        }

        if (newSymbol == null || newSymbol.isBlank()) {

            showError("成績が選択されていません。");

            refresh();

            return;
        }

        /*
         * 選択された成績記号から
         * GradeDefを検索する。
         */
        GradeDef selectedGrade = gradeDefs.stream().filter(grade -> newSymbol.equals(grade.getSymbol())).findFirst().orElse(null);

        if (selectedGrade == null) {

            showError("指定された成績が存在しません: " + newSymbol);

            refresh();

            return;
        }

        try {

            /*
             * カリキュラムIDも渡して更新する。
             *
             * これにより別カリキュラムの履修を
             * 誤って更新することを防ぐ。
             */
            gradeService.updateGradeByCurriculum(curriculum.getId(), display.getEnrollmentId(), selectedGrade.getId());

            /*
             * 更新後の最新データを再読み込みする。
             */
            loadEnrollments(curriculum);

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

            refresh();
        }
    }

    /**
     * 成績一覧を再読み込みする。
     */
    @FXML
    private void refresh() throws SQLException {

        loadEnrollments(curriculumComboBox.getValue());
    }

    /**
     * エラーダイアログを表示する。
     */
    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("エラー");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
