package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractController;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.CurriculumCourseDisplay;
import org.takoyaki.curriculummanager.service.CurriculumCourseService;
import org.takoyaki.curriculummanager.service.CurriculumService;
import java.sql.SQLException;
import java.util.List;

import static org.takoyaki.curriculummanager.util.RequirementTypeUtils.ELECTIVE;
import static org.takoyaki.curriculummanager.util.RequirementTypeUtils.REQUIRED;

public class CurriculumCourseController extends AbstractController {
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;
    @FXML
    private ComboBox<CourseCategory> categoryComboBox;
    @FXML
    private TableView<CurriculumCourseDisplay> courseTable;
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> courseCodeColumn;
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> courseNameColumn;
    @FXML
    private TableColumn<CurriculumCourseDisplay, Number> creditsColumn;
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> requirementTypeColumn;
    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;

    public CurriculumCourseController() {
        curriculumService = new CurriculumService();
        curriculumCourseService = new CurriculumCourseService();
    }

    @FXML
    private void initialize() throws SQLException {
        setupColumns();
        setupRequirementTypeColumn();
        setupDepartmentComboBox();
        setupCurriculumComboBox();
        setupCategoryComboBox();
        loadDepartments();
    }

    private void setupColumns() {
        courseCodeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseCode()));
        courseNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getCredits()));
        requirementTypeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRequirementType()));
    }

    private void setupRequirementTypeColumn() {
        courseTable.setEditable(true);
        requirementTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(REQUIRED, ELECTIVE)));
        requirementTypeColumn.setOnEditCommit(event -> {
            CurriculumCourseDisplay display = event.getRowValue();
            String newRequirementType = event.getNewValue();

            if (display == null) {
                return;
            }

            if (newRequirementType == null) {
                return;
            }

            if (display.getId() == null) {
                showError(I18n.text("curriculumCourse.error.notRegistered"));
                reloadCurrentCategorySafely();
                return;
            }

            String oldRequirementType = display.getRequirementType();

            try {
                display.setRequirementType(newRequirementType);
                CurriculumCourse curriculumCourse = new CurriculumCourse(display.getId(), display.getCurriculumId(), display.getCourseId(), newRequirementType);
                curriculumCourseService.update(curriculumCourse);
                courseTable.refresh();
            } catch (Exception e) {
                display.setRequirementType(oldRequirementType);
                courseTable.refresh();
                showError(I18n.text("curriculumCourse.error.typeUpdate", e.getMessage()));
            }
        });
    }

    private void setupDepartmentComboBox() {
        departmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadCurricula(newValue);
            } catch (SQLException e) {
                showError(I18n.text("curriculumCourse.error.curriculumLoad", e.getMessage()));
            }
        });
    }

    private void setupCurriculumComboBox() {
        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadCategories(newValue);
            } catch (SQLException e) {
                showError(I18n.text("curriculumCourse.error.categoryLoad", e.getMessage()));
            }
        });
    }

    private void setupCategoryComboBox() {
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadCourses(newValue);
            } catch (SQLException e) {
                showError(I18n.text("curriculumCourse.error.courseLoad", e.getMessage()));
            }
        });
    }

    private void loadDepartments() throws SQLException {
        List<Department> departments = curriculumService.getDepartments();
        departmentComboBox.setItems(FXCollections.observableArrayList(departments));

        if (!departments.isEmpty()) {
            departmentComboBox.getSelectionModel().selectFirst();
        } else {
            clearCurricula();
            clearCategories();
            clearCourses();
        }
    }

    private void loadCurricula(Department department) throws SQLException {
        curriculumComboBox.getItems().clear();
        categoryComboBox.getItems().clear();
        courseTable.getItems().clear();

        if (department == null || department.getId() == null) {
            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(department.getId());
        curriculumComboBox.setItems(FXCollections.observableArrayList(curricula));

        if (!curricula.isEmpty()) {
            curriculumComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadCategories(Curriculum curriculum) throws SQLException {
        categoryComboBox.getItems().clear();
        courseTable.getItems().clear();

        if (curriculum == null || curriculum.getId() == null) {
            return;
        }

        List<CourseCategory> categories = curriculumService.getCategories(curriculum.getId());
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        if (!categories.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadCourses(CourseCategory category) throws SQLException {
        courseTable.getItems().clear();
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {
            return;
        }

        if (category == null || category.getId() == null) {
            return;
        }

        List<CurriculumCourseDisplay> displays = curriculumCourseService.getCourseDisplaysByCategory(curriculum.getId(), category.getId());
        courseTable.setItems(FXCollections.observableArrayList(displays));
    }

    @FXML
    private void addCourse() {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {
            showError(I18n.text("curriculumCourse.error.curriculumRequired"));
            return;
        }

        CurriculumCourseDisplay selectedCourse = courseTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showError(I18n.text("curriculumCourse.error.courseRequired"));
            return;
        }

        if (selectedCourse.getId() != null) {
            showError(I18n.text("curriculumCourse.error.alreadyRegistered"));
            return;
        }

        try {
            curriculumCourseService.addCourseToCurriculum(curriculum.getId(), selectedCourse.getCourseId(), ELECTIVE);
            reloadCurrentCategory();
        } catch (Exception e) {
            showError(I18n.text("curriculumCourse.error.add", e.getMessage()));
        }
    }

    @FXML
    private void removeCourse() {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {
            showError(I18n.text("curriculumCourse.error.curriculumRequired"));
            return;
        }

        CurriculumCourseDisplay selectedCourse = courseTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showError(I18n.text("curriculumCourse.error.courseRequired"));
            return;
        }

        if (selectedCourse.getId() == null) {
            showError(I18n.text("curriculumCourse.error.notInCurriculum"));
            return;
        }

        try {
            curriculumCourseService.removeCourseFromCurriculum(curriculum.getId(), selectedCourse.getCourseId());
            reloadCurrentCategory();
        } catch (Exception e) {
            showError(I18n.text("curriculumCourse.error.remove", e.getMessage()));
        }
    }

    private void reloadCurrentCategory() throws SQLException {
        loadCourses(categoryComboBox.getValue());
    }

    private void reloadCurrentCategorySafely() {
        try {
            reloadCurrentCategory();
        } catch (SQLException e) {
            showError(I18n.text("curriculumCourse.error.reload", e.getMessage()));
        }
    }

    private void clearCurricula() {
        curriculumComboBox.getItems().clear();
    }

    private void clearCategories() {
        categoryComboBox.getItems().clear();
    }

    private void clearCourses() {
        courseTable.getItems().clear();
    }

}
