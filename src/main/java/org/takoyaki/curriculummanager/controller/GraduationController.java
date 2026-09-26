package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractAcademicContextController;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.GraduationRequirementDisplay;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;
import org.takoyaki.curriculummanager.repository.GraduationRequirementRepository;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.GraduationRequirementService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GraduationController extends AbstractAcademicContextController {
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private Label academicContextLabel;
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;
    @FXML
    private TableView<GraduationRequirementDisplay> requirementTable;
    @FXML
    private TableColumn<GraduationRequirementDisplay, String> nameColumn;
    @FXML
    private TableColumn<GraduationRequirementDisplay, String> targetCategoriesColumn;
    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> requiredColumn;
    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> earnedColumn;
    @FXML
    private TableColumn<GraduationRequirementDisplay, Number> remainingColumn;
    @FXML
    private TableColumn<GraduationRequirementDisplay, String> statusColumn;
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
        setupCurriculumComboBox();
        loadAcademicContext();
    }

    private void loadAcademicContext() throws SQLException {
        org.takoyaki.curriculummanager.model.Major major = updateAcademicContext(academicContextLabel);
        loadCurricula(major);
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        targetCategoriesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTargetCategories()));
        requiredColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRequiredCredits()));
        earnedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getEarnedCredits()));
        remainingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRemainingCredits()));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isSatisfied() ? "達成" : "未達成"));
    }

    private void setupMandatoryCourseColumns() {
        mandatoryCourseCodeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseCode()));
        mandatoryCourseNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseName()));
        mandatoryCreditsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCredits()));
        mandatoryStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isPassed() ? "修得済み" : "未修得"));
    }

    private void setupCurriculumComboBox() {
        curriculumComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                refresh(newValue);
            } catch (SQLException e) {
                showError("卒業要件の読み込みに失敗しました。\n" + e.getMessage());
            }
        });
    }

    private void loadCurricula(org.takoyaki.curriculummanager.model.Major major) throws SQLException {
        curriculumComboBox.getItems().clear();
        requirementTable.getItems().clear();
        mandatoryCourseTable.getItems().clear();
        clearView();

        if (major == null) {
            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(major.getId());
        var items = FXCollections.<Curriculum>observableArrayList();
        items.add(Curriculum.allOption());
        items.addAll(curricula);
        curriculumComboBox.setItems(items);
        curriculumComboBox.getSelectionModel().selectFirst();
    }

    private void refresh(Curriculum curriculum) throws SQLException {
        requirementTable.getItems().clear();
        mandatoryCourseTable.getItems().clear();

        if (curriculum == null) {
            clearView();
            return;
        }

        if (curriculum.isAllOption()) {
            showAllCurricula();
            return;
        }

        List<GraduationRequirementDisplay> displays = graduationRequirementService.getRequirementDisplays(curriculum.getId());
        requirementTable.setItems(FXCollections.observableArrayList(displays));
        double totalCredits = 0.0;

        for (GraduationRequirementDisplay display : displays) {
            totalCredits += display.getEarnedCredits();
        }

        totalCreditsLabel.setText(String.format("修得単位: %.1f", totalCredits));
        List<MandatoryCourseStatus> mandatoryCourseStatuses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());
        mandatoryCourseTable.setItems(FXCollections.observableArrayList(mandatoryCourseStatuses));
        boolean mandatorySatisfied = graduationRequirementService.isMandatoryCoursesSatisfied(curriculum.getId());
        boolean requirementsSatisfied = graduationRequirementService.areRequirementsSatisfied(curriculum.getId());
        String requirementStatus = displays.isEmpty() ? "未設定" : requirementsSatisfied ? "達成" : "未達成";
        String mandatoryStatus = mandatoryCourseStatuses.isEmpty() ? "対象なし" : mandatorySatisfied ? "達成" : "未達成";
        boolean graduated = requirementsSatisfied && mandatorySatisfied;
        graduationStatusLabel.setText((graduated ? "卒業可能" : "卒業不可") + "（単位要件: " + requirementStatus + " / 必修科目: " + mandatoryStatus + "）");
    }

    private void showAllCurricula() throws SQLException {
        List<GraduationRequirementDisplay> allRequirements = new ArrayList<>();
        List<MandatoryCourseStatus> allMandatoryCourses = new ArrayList<>();
        double totalCredits = 0.0;
        int configuredCount = 0;
        int graduatedCount = 0;
        int requirementsConfiguredCount = 0;
        int requirementsSatisfiedCount = 0;
        int mandatoryConfiguredCount = 0;
        int mandatorySatisfiedCount = 0;

        for (Curriculum item : curriculumComboBox.getItems()) {
            if (item == null || item.isAllOption()) {
                continue;
            }

            List<GraduationRequirementDisplay> requirements = graduationRequirementService.getRequirementDisplays(item.getId());
            List<MandatoryCourseStatus> mandatoryCourses = graduationRequirementService.getMandatoryCourseStatuses(item.getId());

            if (!requirements.isEmpty() || !mandatoryCourses.isEmpty()) {
                configuredCount++;

                if (graduationRequirementService.isGraduated(item.getId())) {
                    graduatedCount++;
                }
            }

            if (!requirements.isEmpty()) {
                requirementsConfiguredCount++;

                if (graduationRequirementService.areRequirementsSatisfied(item.getId())) {
                    requirementsSatisfiedCount++;
                }
            }

            if (!mandatoryCourses.isEmpty()) {
                mandatoryConfiguredCount++;

                if (graduationRequirementService.isMandatoryCoursesSatisfied(item.getId())) {
                    mandatorySatisfiedCount++;
                }
            }

            for (GraduationRequirementDisplay requirement : requirements) {
                allRequirements.add(new GraduationRequirementDisplay(requirement.getRequirementId(), item.getName() + " / " + requirement.getName(), requirement.getTargetCategories(), requirement.getRequiredCredits(), requirement.getEarnedCredits()));
                totalCredits += requirement.getEarnedCredits();
            }

            for (MandatoryCourseStatus course : mandatoryCourses) {
                allMandatoryCourses.add(new MandatoryCourseStatus(course.getCourseId(), course.getCourseCode(), item.getName() + " / " + course.getCourseName(), course.getCredits(), course.isPassed()));
            }
        }

        requirementTable.setItems(FXCollections.observableArrayList(allRequirements));
        mandatoryCourseTable.setItems(FXCollections.observableArrayList(allMandatoryCourses));
        totalCreditsLabel.setText(String.format("修得単位: %.1f", totalCredits));
        graduationStatusLabel.setText(configuredCount == 0 ? "卒業要件が設定されていません" : "卒業可能: " + graduatedCount + " / " + configuredCount + "　単位要件達成: " + requirementsSatisfiedCount + " / " + requirementsConfiguredCount + "　必修達成: " + mandatorySatisfiedCount + " / " + mandatoryConfiguredCount);
    }

    private void clearView() {
        requirementTable.getItems().clear();
        mandatoryCourseTable.getItems().clear();
        graduationStatusLabel.setText("カリキュラム未選択");
        totalCreditsLabel.setText("修得単位: 0.0");
    }

    @FXML
    private void refresh() throws SQLException {
        refresh(curriculumComboBox.getValue());
    }

    @FXML
    private void addRequirement() {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {
            showError("卒業要件を追加するカリキュラムを選択してください。");
            return;
        }

        List<CourseCategory> categories;

        try {
            categories = curriculumService.getCategories(curriculum.getId());
        } catch (SQLException e) {
            showError("科目カテゴリの読み込みに失敗しました。\n" + e.getMessage());
            return;
        }

        Dialog<GraduationRequirement> dialog = new Dialog<>();
        dialog.setTitle("卒業要件の追加");
        dialog.setHeaderText("卒業要件の対象と必要単位数を設定してください。");
        ButtonType saveButtonType = new ButtonType("追加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        TextField nameField = new TextField();
        nameField.setPromptText("例: 基礎科学分野");
        TextField creditsField = new TextField();
        creditsField.setPromptText("例: 14");
        CheckBox allCategoriesCheckBox = new CheckBox("すべてのカテゴリを対象にする");
        allCategoriesCheckBox.setSelected(true);
        ListView<CourseCategory> categoryListView = new ListView<>();
        categoryListView.setItems(FXCollections.observableArrayList(categories));
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        categoryListView.setPrefHeight(220);
        categoryListView.setDisable(true);
        allCategoriesCheckBox.selectedProperty().addListener((observable, oldValue, selected) -> {
            categoryListView.setDisable(selected);

            if (selected) {
                categoryListView.getSelectionModel().clearSelection();
            }
        });
        VBox box = new VBox(10, new Label("要件名"), nameField, new Label("必要単位"), creditsField, new Label("対象"), allCategoriesCheckBox, new Label("対象カテゴリ" + "（Ctrlキーを押しながらクリックすると複数選択できます）"), categoryListView);
        box.setPrefWidth(450);
        dialog.getDialogPane().setContent(box);
        Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButtonNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                showError("要件名を入力してください。");
                event.consume();
                return;
            }

            double credits;

            try {
                credits = Double.parseDouble(creditsField.getText().trim());
            } catch (NumberFormatException e) {
                showError("必要単位には数値を入力してください。");
                event.consume();
                return;
            }

            if (credits < 0) {
                showError("必要単位は0以上で指定してください。");
                event.consume();
                return;
            }

            if (!allCategoriesCheckBox.isSelected() && categoryListView.getSelectionModel().getSelectedItems().isEmpty()) {
                showError("対象カテゴリを1つ以上選択してください。\n" + "カリキュラム全体を対象にする場合は" + "「すべてのカテゴリを対象にする」" + "を選択してください。");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return null;
            }

            String name = nameField.getText().trim();
            double credits = Double.parseDouble(creditsField.getText().trim());
            List<Integer> categoryIds = new ArrayList<>();

            if (!allCategoriesCheckBox.isSelected()) {
                for (CourseCategory category : categoryListView.getSelectionModel().getSelectedItems()) {
                    categoryIds.add(category.getId());
                }
            }

            return new GraduationRequirement(curriculum.getId(), categoryIds, name, credits);
        });
        dialog.showAndWait().ifPresent(this::saveRequirement);
    }

    @FXML
    private void editRequirement() {
        Curriculum curriculum = curriculumComboBox.getValue();
        GraduationRequirementDisplay selected = requirementTable.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption() || selected == null || selected.getRequirementId() == null) {
            showError("編集する卒業要件を選択してください。");
            return;
        }

        try {
            GraduationRequirementRepository repository = new GraduationRequirementRepository();
            GraduationRequirement requirement = repository.findById(selected.getRequirementId());

            if (requirement == null) {
                showError("選択した卒業要件が見つかりません。");
                return;
            }

            List<CourseCategory> categories = curriculumService.getCategories(curriculum.getId());
            Dialog<GraduationRequirement> dialog = new Dialog<>();
            dialog.setTitle("卒業要件の編集");
            dialog.setHeaderText("卒業要件の内容を変更してください。");
            ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            TextField nameField = new TextField(requirement.getName());
            TextField creditsField = new TextField(String.valueOf(requirement.getRequiredCredits()));
            CheckBox allCategoriesCheckBox = new CheckBox("すべてのカテゴリを対象にする");
            allCategoriesCheckBox.setSelected(requirement.isAllCategories());
            ListView<CourseCategory> categoryListView = new ListView<>(FXCollections.observableArrayList(categories));
            categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            categoryListView.setPrefHeight(220);
            categoryListView.setDisable(requirement.isAllCategories());

            if (!requirement.isAllCategories()) {
                for (CourseCategory category : categories) {
                    if (requirement.getCategoryIds().contains(category.getId())) {
                        categoryListView.getSelectionModel().select(category);
                    }
                }
            }

            allCategoriesCheckBox.selectedProperty().addListener((observable, oldValue, selectedAll) -> {
                categoryListView.setDisable(selectedAll);

                if (selectedAll) {
                    categoryListView.getSelectionModel().clearSelection();
                }
            });
            VBox box = new VBox(10, new Label("要件名"), nameField, new Label("必要単位"), creditsField, new Label("対象"), allCategoriesCheckBox, new Label("対象カテゴリ（Ctrlキーを押しながらクリックすると複数選択できます）"), categoryListView);
            box.setPrefWidth(450);
            dialog.getDialogPane().setContent(box);
            Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButtonNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (nameField.getText().isBlank()) {
                    showError("要件名を入力してください。");
                    event.consume();
                    return;
                }

                try {
                    double credits = Double.parseDouble(creditsField.getText().trim());

                    if (!Double.isFinite(credits) || credits < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    showError("必要単位は0以上の数値で入力してください。");
                    event.consume();
                    return;
                }

                if (!allCategoriesCheckBox.isSelected() && categoryListView.getSelectionModel().getSelectedItems().isEmpty()) {
                    showError("対象カテゴリを1つ以上選択してください。");
                    event.consume();
                }
            });
            dialog.setResultConverter(button -> {
                if (button != saveButtonType) {
                    return null;
                }

                List<Integer> categoryIds = new ArrayList<>();

                if (!allCategoriesCheckBox.isSelected()) {
                    for (CourseCategory category : categoryListView.getSelectionModel().getSelectedItems()) {
                        categoryIds.add(category.getId());
                    }
                }

                return new GraduationRequirement(requirement.getId(), curriculum.getId(), categoryIds, nameField.getText().trim(), Double.parseDouble(creditsField.getText().trim()));
            });
            dialog.showAndWait().ifPresent(updated -> {
                try {
                    repository.update(updated);
                    refresh(curriculum);
                } catch (SQLException e) {
                    showError("卒業要件の更新に失敗しました。\n" + e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("卒業要件の読み込みに失敗しました。\n" + e.getMessage());
        }
    }

    private void saveRequirement(GraduationRequirement requirement) {
        try {
            GraduationRequirementRepository repository = new GraduationRequirementRepository();
            repository.save(requirement);
            refresh(curriculumComboBox.getValue());
        } catch (SQLException e) {
            showError("卒業要件の保存に失敗しました。\n" + e.getMessage());
        }
    }

}
