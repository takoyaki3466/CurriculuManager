package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.GraduationRequirementDisplay;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;
import org.takoyaki.curriculummanager.repository.GraduationRequirementRepository;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.GraduationRequirementService;

import java.sql.SQLException;
import java.util.List;

/**
 * 卒業要件画面を管理するコントローラー。
 */
public class GraduationController {

    @FXML
    private ComboBox<Department> departmentComboBox;

    @FXML
    private ComboBox<Curriculum> curriculumComboBox;

    @FXML
    private TableView<GraduationRequirementDisplay> requirementTable;

    @FXML
    private TableColumn<GraduationRequirementDisplay, String> nameColumn;

    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> requiredColumn;

    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> earnedColumn;

    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> remainingColumn;

    @FXML
    private TableColumn<GraduationRequirementDisplay, String> statusColumn;

    /*
     * 必修科目一覧
     */
    @FXML
    private TableView<MandatoryCourseStatus> mandatoryCourseTable;

    @FXML
    private TableColumn<MandatoryCourseStatus, String> mandatoryCourseCodeColumn;

    @FXML
    private TableColumn<MandatoryCourseStatus, String> mandatoryCourseNameColumn;

    @FXML
    private TableColumn<MandatoryCourseStatus, Number> mandatoryCreditsColumn;

    @FXML
    private TableColumn<MandatoryCourseStatus, String> mandatoryStatusColumn;

    @FXML
    private Label graduationStatusLabel;

    @FXML
    private Label totalCreditsLabel;

    private final CurriculumService curriculumService;

    private final GraduationRequirementService graduationRequirementService;

    public GraduationController() {

        curriculumService = new CurriculumService();

        graduationRequirementService = new GraduationRequirementService();
    }

    @FXML
    private void initialize() throws SQLException {

        setupColumns();

        setupMandatoryCourseColumns();

        setupDepartmentComboBox();

        setupCurriculumComboBox();

        loadDepartments();
    }

    /**
     * 卒業要件テーブルの列を設定する。
     */
    private void setupColumns() {

        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        requiredColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRequiredCredits()));

        earnedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getEarnedCredits()));

        remainingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRemainingCredits()));

        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isSatisfied() ? "達成" : "未達成"));
    }

    /**
     * 必修科目テーブルの列を設定する。
     */
    private void setupMandatoryCourseColumns() {

        /*
         * 科目コード
         */
        mandatoryCourseCodeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseCode()));

        /*
         * 科目名
         */
        mandatoryCourseNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseName()));

        /*
         * 単位数
         */
        mandatoryCreditsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCredits()));

        /*
         * 修得状況
         */
        mandatoryStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isPassed() ? "修得済み" : "未修得"));
    }

    /**
     * 学部選択時の処理を設定する。
     */
    private void setupDepartmentComboBox() {

        departmentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            try {

                loadCurricula(newValue);

            } catch (SQLException e) {

                showError("カリキュラムの読み込みに失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * カリキュラム選択時の処理を設定する。
     */
    private void setupCurriculumComboBox() {

        curriculumComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            try {

                refresh(newValue);

            } catch (SQLException e) {

                showError("卒業要件の読み込みに失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * 学部一覧を読み込む。
     */
    private void loadDepartments() throws SQLException {

        List<Department> departments = curriculumService.getDepartments();

        departmentComboBox.setItems(FXCollections.observableArrayList(departments));
    }

    /**
     * 選択された学部のカリキュラムを読み込む。
     */
    private void loadCurricula(Department department) throws SQLException {

        curriculumComboBox.getItems().clear();

        requirementTable.getItems().clear();

        mandatoryCourseTable.getItems().clear();

        clearView();

        if (department == null) {
            return;
        }

        List<Curriculum> curricula = curriculumService.getAllCurricula().stream().filter(curriculum -> {

            var major = curriculumService.getMajor(curriculum.getMajorId());

            return major != null && department.getId().equals(major.getDepartmentId());
        }).toList();

        curriculumComboBox.setItems(FXCollections.observableArrayList(curricula));
    }

    /**
     * 選択されたカリキュラムの
     * 卒業要件と必修科目を読み込む。
     */
    private void refresh(Curriculum curriculum) throws SQLException {

        requirementTable.getItems().clear();

        mandatoryCourseTable.getItems().clear();

        if (curriculum == null) {

            clearView();

            return;
        }

        /*
         * 卒業要件を読み込む。
         */
        List<GraduationRequirementDisplay> displays = graduationRequirementService.getRequirementDisplays(curriculum.getId());

        requirementTable.setItems(FXCollections.observableArrayList(displays));

        /*
         * 現在の実装では、
         * 卒業要件ごとの修得単位を合計して表示する。
         *
         * 同じ科目が複数の要件に含まれる場合には
         * 二重計上される可能性があるため、
         * 「全体の正確な修得単位数」ではない。
         */
        double totalCredits = 0.0;

        for (GraduationRequirementDisplay display : displays) {

            totalCredits += display.getEarnedCredits();
        }

        totalCreditsLabel.setText(String.format("修得単位: %.1f", totalCredits));

        /*
         * 必修科目を読み込む。
         */
        List<MandatoryCourseStatus> mandatoryCourseStatuses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());

        mandatoryCourseTable.setItems(FXCollections.observableArrayList(mandatoryCourseStatuses));

        /*
         * 必修科目の修得状況を確認する。
         */
        boolean mandatorySatisfied = graduationRequirementService.isMandatoryCoursesSatisfied(curriculum.getId());

        /*
         * 卒業要件全体を確認する。
         */
        boolean requirementsSatisfied = graduationRequirementService.isGraduated(curriculum.getId());

        if (requirementsSatisfied) {

            graduationStatusLabel.setText("卒業要件達成");

        } else if (!mandatorySatisfied) {

            graduationStatusLabel.setText("必修科目が未修得です");

        } else {

            graduationStatusLabel.setText("卒業要件未達成");
        }
    }

    /**
     * 画面を初期状態に戻す。
     */
    private void clearView() {

        requirementTable.getItems().clear();

        mandatoryCourseTable.getItems().clear();

        graduationStatusLabel.setText("カリキュラム未選択");

        totalCreditsLabel.setText("修得単位: 0.0");
    }

    /**
     * 更新ボタンから呼び出される。
     */
    @FXML
    private void refresh() throws SQLException {

        refresh(curriculumComboBox.getValue());
    }

    /**
     * 卒業要件を追加する。
     */
    @FXML
    private void addRequirement() {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null) {

            showError("カリキュラムを選択してください。");

            return;
        }

        Dialog<GraduationRequirement> dialog = new Dialog<>();

        dialog.setTitle("卒業要件の追加");

        ButtonType saveButton = new ButtonType("追加", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField();

        TextField creditsField = new TextField();

        VBox box = new VBox(10, new Label("要件名"), nameField, new Label("必要単位"), creditsField);

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(button -> {

            if (button != saveButton) {
                return null;
            }

            String name = nameField.getText().trim();

            if (name.isEmpty()) {

                showError("要件名を入力してください。");

                return null;
            }

            double credits;

            try {

                credits = Double.parseDouble(creditsField.getText().trim());

            } catch (NumberFormatException e) {

                showError("必要単位には数値を入力してください。");

                return null;
            }

            if (credits < 0) {

                showError("必要単位は0以上で指定してください。");

                return null;
            }

            return new GraduationRequirement(curriculum.getId(), null, name, credits);
        });

        dialog.showAndWait().ifPresent(this::saveRequirement);
    }

    /**
     * 卒業要件を保存する。
     */
    private void saveRequirement(GraduationRequirement requirement) {

        try {

            GraduationRequirementRepository repository = new GraduationRequirementRepository();

            repository.save(requirement);

            refresh(curriculumComboBox.getValue());

        } catch (SQLException e) {

            showError("卒業要件の保存に失敗しました。\n" + e.getMessage());
        }
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
