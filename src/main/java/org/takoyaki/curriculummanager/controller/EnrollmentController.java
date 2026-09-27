package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractAcademicContextController;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.EnrollmentDisplay;
import org.takoyaki.curriculummanager.service.CurriculumCourseService;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.EnrollmentService;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class EnrollmentController extends AbstractAcademicContextController {
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private javafx.scene.control.Label academicContextLabel;
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;
    @FXML
    private ComboBox<Course> courseComboBox;
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<String> semesterComboBox;
    @FXML
    private TableView<EnrollmentDisplay> enrollmentTable;
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
    private final CurriculumCourseService curriculumCourseService;
    private final EnrollmentService enrollmentService;

    public EnrollmentController() {
        curriculumService = new CurriculumService();
        curriculumCourseService = new CurriculumCourseService();
        enrollmentService = new EnrollmentService();
    }

    @FXML
    private void initialize() throws SQLException {
        setupColumns();
        setupSemesterComboBox();
        setupYearComboBox();
        setupCurriculumComboBox();
        setupCourseComboBox();
        loadAcademicContext();
    }

    private void loadAcademicContext() throws SQLException {
        org.takoyaki.curriculummanager.model.Major major = updateAcademicContext(academicContextLabel);
        loadCurricula(major);
    }

    private void setupColumns() {
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("gradeSymbol"));
    }

    private void setupSemesterComboBox() {
        semesterComboBox.setItems(FXCollections.observableArrayList(
                I18n.text("enrollment.semester.first"),
                I18n.text("enrollment.semester.second")
        ));
        semesterComboBox.getSelectionModel().selectFirst();
    }

    private void setupYearComboBox() {
        int currentYear = Year.now().getValue();
        List<Integer> years = java.util.stream.IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList();
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.setValue(currentYear);
    }

    private void setupCurriculumComboBox() {
        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveCurrentCurriculum(newValue);

            try {
                loadCourses(newValue);
                loadEnrollments(newValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupCourseComboBox() {
        courseComboBox.setItems(FXCollections.observableArrayList());
    }

    private void loadCurricula(org.takoyaki.curriculummanager.model.Major major) throws SQLException {
        if (major == null || major.getId() == null) {
            curriculumComboBox.getItems().clear();
            courseComboBox.getItems().clear();
            enrollmentTable.getItems().clear();
            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(major.getId());
        var items = FXCollections.<Curriculum>observableArrayList();
        items.add(Curriculum.allOption());
        items.addAll(curricula);
        curriculumComboBox.setItems(items);

        if (!items.isEmpty()) {
            selectCurrentCurriculum(curriculumComboBox);
        } else {
            courseComboBox.getItems().clear();
            enrollmentTable.getItems().clear();
        }
    }

    private void loadCourses(Curriculum curriculum) throws SQLException {
        courseComboBox.getItems().clear();

        if (curriculum == null || curriculum.getId() == null) {
            return;
        }

        if (curriculum.isAllOption()) {
            return;
        }

        List<Course> courses = curriculumCourseService.getCourses(curriculum.getId());
        courseComboBox.setItems(FXCollections.observableArrayList(courses));
    }

    private void loadEnrollments(Curriculum curriculum) throws SQLException {
        if (curriculum == null || curriculum.getId() == null) {
            enrollmentTable.getItems().clear();
            return;
        }

        if (curriculum.isAllOption()) {
            List<EnrollmentDisplay> displays = new java.util.ArrayList<>();

            for (Curriculum item : curriculumComboBox.getItems()) {
                if (item != null && !item.isAllOption()) {
                    displays.addAll(enrollmentService.getEnrollmentDisplaysByCurriculum(item.getId()));
                }
            }

            enrollmentTable.setItems(FXCollections.observableArrayList(displays));
            return;
        }

        List<EnrollmentDisplay> displays = enrollmentService.getEnrollmentDisplaysByCurriculum(curriculum.getId());
        enrollmentTable.setItems(FXCollections.observableArrayList(displays));
    }

    @FXML
    private void addEnrollment() {
        Curriculum curriculum = curriculumComboBox.getValue();
        Course course = courseComboBox.getValue();
        Integer year = yearComboBox.getValue();
        String semester = semesterComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {
            showError(I18n.text("enrollment.error.curriculumRequired"));
            return;
        }

        if (course == null) {
            showError(I18n.text("enrollment.error.courseRequired"));
            return;
        }

        if (year == null) {
            showError(I18n.text("enrollment.error.yearRequired"));
            return;
        }

        if (semester == null || semester.isBlank()) {
            showError(I18n.text("enrollment.error.semesterRequired"));
            return;
        }

        try {
            if (!curriculumCourseService.exists(curriculum.getId(), course.getId())) {
                showError(I18n.text("enrollment.error.courseNotRegistered"));
                return;
            }

            enrollmentService.addEnrollment(curriculum.getId(), course.getId(), year, semester);
            loadEnrollments(curriculum);
            courseComboBox.getSelectionModel().clearSelection();
            showInformation(I18n.text("enrollment.success.registered"));
        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void deleteEnrollment() {
        EnrollmentDisplay selected = enrollmentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError(I18n.text("enrollment.error.deleteSelection"));
            return;
        }

        try {
            enrollmentService.deleteEnrollment(selected.getEnrollmentId());
            loadEnrollments(curriculumComboBox.getValue());
        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void refresh() throws SQLException {
        loadEnrollments(curriculumComboBox.getValue());
    }

}
