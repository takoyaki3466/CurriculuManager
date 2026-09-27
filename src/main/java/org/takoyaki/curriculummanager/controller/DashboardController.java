package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractAcademicContextController;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.YearlyAcademicSummary;
import org.takoyaki.curriculummanager.service.CreditCalculationService;
import org.takoyaki.curriculummanager.service.DashboardService;
import org.takoyaki.curriculummanager.service.GpaCalculationService;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.view.dialog.AppDialogs;
import java.sql.SQLException;
import java.util.List;

public class DashboardController extends AbstractAcademicContextController {
    @FXML
    private Label gpaLabel;
    @FXML
    private Label earnedCreditsLabel;
    @FXML
    private Label graduationPossibleLabel;
    @FXML
    private Label academicContextLabel;
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private TableView<YearlyAcademicSummary> yearlySummaryTable;
    @FXML
    private TableColumn<YearlyAcademicSummary, Number> yearColumn;
    @FXML
    private TableColumn<YearlyAcademicSummary, String> yearlyGpaColumn;
    @FXML
    private TableColumn<YearlyAcademicSummary, String> yearlyCreditsColumn;
    @FXML
    private ListView<String> curriculumRequirementListView;
    private final GpaCalculationService gpaCalculationService;
    private final CreditCalculationService creditCalculationService;
    private final DashboardService dashboardService;
    private final CurriculumService curriculumService;
    private boolean loadingAcademicContext;

    public DashboardController() {
        this.gpaCalculationService = new GpaCalculationService();
        this.creditCalculationService = new CreditCalculationService();
        this.dashboardService = new DashboardService();
        this.curriculumService = new CurriculumService();
    }

    @FXML
    private void initialize() throws SQLException {
        setupYearlySummaryTable();
        setupRequirementList();
        setupAcademicContext();
        refresh();
    }

    private void setupRequirementList() {
        curriculumRequirementListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                boolean incomplete = !empty && item != null
                        && (item.stripLeading().startsWith(I18n.text("dashboard.status.unmet"))
                        || item.stripLeading().startsWith(I18n.text("dashboard.graduationImpossible")));
                setStyle(incomplete
                        ? "-fx-border-color: #d32f2f; -fx-border-width: 1.5; -fx-border-radius: 4; -fx-background-color: #ffebee;"
                        : "");
            }
        });
    }

    private void setupAcademicContext() throws SQLException {
        loadingAcademicContext = true;
        departmentComboBox.setItems(FXCollections.observableArrayList(curriculumService.getDepartments()));
        departmentComboBox.valueProperty().addListener((observable, oldValue, department) -> loadMajors(department));
        majorComboBox.valueProperty().addListener((observable, oldValue, major) -> {
            if (loadingAcademicContext || major == null) {
                return;
            }

            Department department = departmentComboBox.getValue();

            if (department != null) {
                academicContextService.setCurrent(department, major);

                try {
                    refresh();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Department currentDepartment = academicContextService.getCurrentDepartment();
        Major currentMajor = academicContextService.getCurrentMajor();
        departmentComboBox.setValue(currentDepartment);
        loadMajors(currentDepartment);
        majorComboBox.setValue(currentMajor);
        loadingAcademicContext = false;
        updateAcademicContextLabel();
    }

    private void loadMajors(Department department) {
        boolean wasLoading = loadingAcademicContext;
        loadingAcademicContext = true;
        majorComboBox.getItems().clear();

        if (department != null) {
            majorComboBox.setItems(FXCollections.observableArrayList(curriculumService.getMajors(department.getId())));
        }

        loadingAcademicContext = wasLoading;
    }

    private void updateAcademicContextLabel() {
        updateAcademicContext(academicContextLabel);
    }

    @FXML
    private void addDepartment() {
        TextInputDialog dialog = AppDialogs.textInput(
                null,
                I18n.text("dashboard.department.add.title"),
                I18n.text("dashboard.department.add.header"),
                I18n.text("dashboard.department.name")
        );
        dialog.showAndWait().map(String::trim).filter(name -> !name.isBlank()).ifPresent(name -> {
            try {
                Department department = new Department(name);
                curriculumService.addDepartment(department);
                departmentComboBox.getItems().add(department);
                departmentComboBox.setValue(department);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    private void addMajor() {
        Department department = departmentComboBox.getValue();

        if (department == null) {
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                null,
                I18n.text("dashboard.major.add.title"),
                I18n.text("dashboard.major.add.header", department.getName()),
                I18n.text("dashboard.major.name")
        );
        dialog.showAndWait().map(String::trim).filter(name -> !name.isBlank()).ifPresent(name -> {
            Major major = new Major(department.getId(), name);
            curriculumService.addMajor(major);
            majorComboBox.getItems().add(major);
            majorComboBox.setValue(major);
        });
    }

    private void setupYearlySummaryTable() {
        yearColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getYear()));
        yearlyGpaColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getGpa())));
        yearlyCreditsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                I18n.text("dashboard.creditsFormat", String.format("%.1f", data.getValue().getEarnedCredits()))
        ));
    }

    @FXML
    private void refresh() throws SQLException {
        updateAcademicContextLabel();
        List<Enrollment> currentEnrollments = dashboardService.getCurrentEnrollments();
        double gpa = gpaCalculationService.calculateGpa(currentEnrollments);
        double earnedCredits = creditCalculationService.calculateEarnedCredits(currentEnrollments);
        gpaLabel.setText(String.format("%.2f", gpa));
        earnedCreditsLabel.setText(I18n.text("dashboard.creditsFormat", String.format("%.1f", earnedCredits)));
        graduationPossibleLabel.setText(dashboardService.isGraduationPossible()
                ? I18n.text("dashboard.graduationPossible")
                : I18n.text("dashboard.graduationImpossible"));
        List<YearlyAcademicSummary> yearlySummaries = new java.util.ArrayList<>();

        for (Integer year : dashboardService.getEnrollmentYears()) {
            yearlySummaries.add(new YearlyAcademicSummary(
                    year,
                    gpaCalculationService.calculateGpa(currentEnrollments.stream()
                            .filter(enrollment -> enrollment.getYear() == year).toList()),
                    creditCalculationService.calculateEarnedCredits(currentEnrollments.stream()
                            .filter(enrollment -> enrollment.getYear() == year).toList())));
        }

        yearlySummaryTable.setItems(FXCollections.observableArrayList(yearlySummaries));
        List<String> curriculumRequirements = dashboardService.getCurriculumRequirementList();
        curriculumRequirementListView.setItems(FXCollections.observableArrayList(curriculumRequirements));
    }
}
