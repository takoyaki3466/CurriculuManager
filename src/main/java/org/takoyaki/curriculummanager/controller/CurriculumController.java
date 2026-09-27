package org.takoyaki.curriculummanager.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.takoyaki.curriculummanager.controller.abstracts.AbstractAcademicContextController;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.service.CourseCategoryService;
import org.takoyaki.curriculummanager.service.CourseService;
import org.takoyaki.curriculummanager.service.CurriculumCourseService;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.DepartmentService;
import org.takoyaki.curriculummanager.view.dialog.AppAlerts;
import org.takoyaki.curriculummanager.view.dialog.AppDialogs;

import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.takoyaki.curriculummanager.util.RequirementTypeUtils.ELECTIVE;
import static org.takoyaki.curriculummanager.util.RequirementTypeUtils.REQUIRED;

public class CurriculumController extends AbstractAcademicContextController {
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;
    @FXML
    private ListView<CourseCategory> categoryList;
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> courseCodeColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, String> creditsColumn;
    @FXML
    private TableColumn<Course, String> requirementTypeColumn;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label academicContextLabel;
    private final DepartmentService departmentService;
    private final CurriculumService service;
    private final CourseCategoryService courseCategoryService;
    private final CourseService courseService;
    private final CurriculumCourseService curriculumCourseService;
    private final Map<Integer, String> requirementTypesByCourseId = new HashMap<>();

    public CurriculumController() {
        departmentService = new DepartmentService();
        service = new CurriculumService();
        courseCategoryService = new CourseCategoryService();
        courseService = new CourseService();
        curriculumCourseService = new CurriculumCourseService();
    }

    @FXML
    public void initialize() {
        setupCourseTable();
        setupSelectionListeners();
        loadAcademicContext();
    }

    private void setupCourseTable() {
        courseCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));
        courseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        creditsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCredits())));
        requirementTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(requirementTypesByCourseId.getOrDefault(cellData.getValue().getId(), "")));
        courseTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showCourseDescription(newValue));
    }

    private void setupSelectionListeners() {
        curriculumComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            saveCurrentCurriculum(newValue);
            loadCategories(newValue);
        });
        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadCourses(newValue));
    }

    private void loadAcademicContext() {
        Major major = updateAcademicContext(academicContextLabel);
        loadCurricula(major);
    }

    private void loadDepartments() {
        List<Department> departments;

        try {
            departments = departmentService.getDepartments();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        departmentComboBox.setItems(FXCollections.observableArrayList(departments));
        majorComboBox.getItems().clear();
        curriculumComboBox.getItems().clear();
        categoryList.getItems().clear();
        courseTable.getItems().clear();
        descriptionArea.clear();
    }

    @FXML
    private void addDepartment() {
        TextInputDialog dialog = AppDialogs.textInput(
                null,
                I18n.text("curriculum.department.add.title"),
                I18n.text("curriculum.department.add.header"),
                I18n.text("curriculum.department.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(I18n.text("curriculum.error.departmentName"));
            return;
        }

        try {
            Department department = new Department(name);
            departmentService.addDepartment(department);
            loadDepartments();
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.departmentAdd"), e);
        }
    }

    @FXML
    private void editDepartment() {
        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {
            showError(I18n.text("curriculum.error.departmentEditSelection"));
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                department.getName(),
                I18n.text("curriculum.department.edit.title"),
                I18n.text("curriculum.department.edit.header"),
                I18n.text("curriculum.department.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(I18n.text("curriculum.error.departmentName"));
            return;
        }

        try {
            department.setName(name);
            departmentService.updateDepartment(department);
            Integer departmentId = department.getId();
            loadDepartments();
            selectDepartment(departmentId);
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.departmentUpdate"), e);
        }
    }

    @FXML
    private void deleteDepartment() {
        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {
            showError(I18n.text("curriculum.error.departmentDeleteSelection"));
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("curriculum.department.delete.title"),
                I18n.text("curriculum.department.delete.header"),
                I18n.text("curriculum.department.delete.content", department.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            departmentService.deleteDepartment(department.getId());
            loadDepartments();
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.departmentDelete"), e);
        }
    }

    private void loadMajors(Department department) {
        majorComboBox.getItems().clear();
        curriculumComboBox.getItems().clear();
        categoryList.getItems().clear();
        courseTable.getItems().clear();
        descriptionArea.clear();
        requirementTypesByCourseId.clear();

        if (department == null || department.getId() == null) {
            return;
        }

        List<Major> majors = service.getMajors(department.getId());
        majorComboBox.setItems(FXCollections.observableArrayList(majors));
    }

    @FXML
    private void addMajor() {
        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {
            showError(I18n.text("curriculum.error.departmentFirst"));
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                null,
                I18n.text("curriculum.major.add.title"),
                I18n.text("curriculum.major.add.header", department.getName()),
                I18n.text("curriculum.major.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(I18n.text("curriculum.error.majorName"));
            return;
        }

        try {
            Major major = new Major(department.getId(), name);
            service.addMajor(major);
            loadMajors(department);
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.majorAdd"), e);
        }
    }

    private void loadCurricula(Major major) {
        curriculumComboBox.getItems().clear();
        categoryList.getItems().clear();
        courseTable.getItems().clear();
        descriptionArea.clear();

        if (major == null || major.getId() == null) {
            return;
        }

        List<Curriculum> curricula = service.getCurricula(major.getId());
        var items = FXCollections.<Curriculum>observableArrayList();
        items.add(Curriculum.allOption());
        items.addAll(curricula);
        curriculumComboBox.setItems(items);
        selectCurrentCurriculum(curriculumComboBox);
    }

    @FXML
    private void addCurriculum() {
        Major major = academicContextService.getCurrentMajor();

        if (major == null) {
            showError(I18n.text("curriculum.error.majorFirst"));
            return;
        }

        TextInputDialog nameDialog = AppDialogs.textInput(
                null,
                I18n.text("curriculum.curriculum.add.title"),
                I18n.text("curriculum.curriculum.add.header"),
                I18n.text("curriculum.curriculum.name")
        );
        Optional<String> nameResult = nameDialog.showAndWait();

        if (nameResult.isEmpty()) {
            return;
        }

        String name = nameResult.get().trim();

        if (name.isBlank()) {
            showError(I18n.text("curriculum.error.curriculumName"));
            return;
        }

        TextInputDialog yearDialog = AppDialogs.textInput(
                String.valueOf(java.time.Year.now().getValue()),
                I18n.text("curriculum.curriculum.add.title"),
                I18n.text("curriculum.curriculum.startYear.header"),
                I18n.text("curriculum.curriculum.startYear")
        );
        Optional<String> yearResult = yearDialog.showAndWait();

        if (yearResult.isEmpty()) {
            return;
        }

        int startYear;

        try {
            startYear = Integer.parseInt(yearResult.get().trim());
        } catch (NumberFormatException e) {
            showError(I18n.text("curriculum.error.startYear"));
            return;
        }

        try {
            Curriculum curriculum = new Curriculum(major.getId(), name, startYear);
            service.addCurriculum(curriculum);
            loadCurricula(major);
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.curriculumAdd"), e);
        }
    }

    @FXML
    private void editCurriculum() {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {
            showError(
                    I18n.text("curriculum.curriculum.edit.title"),
                    new Exception(I18n.text("curriculum.error.curriculumEditSelection"))
            );
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                curriculum.getName(),
                I18n.text("curriculum.curriculum.edit.title"),
                I18n.text("curriculum.curriculum.edit.header"),
                I18n.text("curriculum.curriculum.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(
                    I18n.text("curriculum.curriculum.edit.title"),
                    new Exception(I18n.text("curriculum.error.curriculumName"))
            );
            return;
        }

        try {
            Integer curriculumId = curriculum.getId();
            curriculum.setName(name);
            service.updateCurriculum(curriculum);
            Major major = academicContextService.getCurrentMajor();

            if (major != null) {
                loadCurricula(major);
                selectCurriculumById(curriculumId);
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.curriculum.edit.title"),
                    new Exception(I18n.text("curriculum.error.curriculumUpdate", e.getMessage()))
            );
        }
    }

    @FXML
    private void deleteCurriculum() {
        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {
            showError(
                    I18n.text("curriculum.curriculum.delete.title"),
                    new Exception(I18n.text("curriculum.error.curriculumDeleteSelection"))
            );
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("curriculum.curriculum.delete.title"),
                I18n.text("curriculum.curriculum.delete.header"),
                I18n.text("curriculum.curriculum.delete.content", curriculum.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            Major major = academicContextService.getCurrentMajor();
            service.deleteCurriculum(curriculum.getId());

            if (major != null) {
                loadCurricula(major);
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.curriculum.delete.title"),
                    new Exception(I18n.text("curriculum.error.curriculumDelete", e.getMessage()))
            );
        }
    }

    private void selectCurriculumById(Integer curriculumId) {
        if (curriculumId == null) {
            return;
        }

        for (Curriculum curriculum : curriculumComboBox.getItems()) {
            if (curriculumId.equals(curriculum.getId())) {
                curriculumComboBox.setValue(curriculum);
                return;
            }
        }
    }

    private void loadCategories(Curriculum curriculum) {
        categoryList.getItems().clear();
        courseTable.getItems().clear();
        descriptionArea.clear();

        if (curriculum == null || curriculum.getId() == null) {
            return;
        }

        List<CourseCategory> categories;

        try {
            if (curriculum.isAllOption()) {
                Major major = academicContextService.getCurrentMajor();
                List<Integer> curriculumIds = major == null
                        ? List.of()
                        : service.getCurricula(major.getId()).stream().map(Curriculum::getId).toList();
                categories = courseCategoryService.getAllCategories().stream()
                        .filter(category -> curriculumIds.contains(category.getCurriculumId()))
                        .toList();
            } else {
                categories = courseCategoryService.getCategories(curriculum.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("curriculum.error.categoryLoad"), e);
        }

        categoryList.setItems(FXCollections.observableArrayList(categories));

        if (curriculum.isAllOption()) {
            List<Integer> categoryIds = categories.stream().map(CourseCategory::getId).toList();
            List<Course> courses = courseService.getAllCourses().stream()
                    .filter(course -> categoryIds.contains(course.getCategoryId()))
                    .toList();
            courseTable.setItems(FXCollections.observableArrayList(courses));
        }
    }

    @FXML
    private void addCategory() {
        Curriculum curriculum = curriculumComboBox.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption()) {
            showError(I18n.text("curriculum.error.curriculumTarget"));
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                null,
                I18n.text("curriculum.category.add.title"),
                I18n.text("curriculum.category.add.header"),
                I18n.text("curriculum.category.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(I18n.text("curriculum.error.categoryName"));
            return;
        }

        try {
            CourseCategory category = new CourseCategory(curriculum.getId(), null, name, 0);
            courseCategoryService.addCategory(category);
            loadCategories(curriculum);
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.categoryAdd"), e);
        }
    }

    @FXML
    private void editCategory() {
        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (category == null) {
            showError(
                    I18n.text("curriculum.category.edit.title"),
                    new Exception(I18n.text("curriculum.error.categoryEditSelection"))
            );
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                category.getName(),
                I18n.text("curriculum.category.edit.title"),
                I18n.text("curriculum.category.edit.header"),
                I18n.text("curriculum.category.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(
                    I18n.text("curriculum.category.edit.title"),
                    new Exception(I18n.text("curriculum.error.categoryName"))
            );
            return;
        }

        try {
            Integer categoryId = category.getId();
            category.setName(name);
            courseCategoryService.updateCategory(category);
            Curriculum curriculum = curriculumComboBox.getValue();

            if (curriculum != null) {
                loadCategories(curriculum);
                selectCategoryById(categoryId);
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.category.edit.title"),
                    new Exception(I18n.text("curriculum.error.categoryUpdate", e.getMessage()))
            );
        }
    }

    @FXML
    private void deleteCategory() {
        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (category == null) {
            showError(
                    I18n.text("curriculum.category.delete.title"),
                    new Exception(I18n.text("curriculum.error.categoryDeleteSelection"))
            );
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("curriculum.category.delete.title"),
                I18n.text("curriculum.category.delete.header"),
                I18n.text("curriculum.category.delete.content", category.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            Curriculum curriculum = curriculumComboBox.getValue();
            courseCategoryService.deleteCategory(category.getId());

            if (curriculum != null) {
                loadCategories(curriculum);
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.category.delete.title"),
                    new Exception(I18n.text("curriculum.error.categoryDelete", e.getMessage()))
            );
        }
    }

    private void selectCategoryById(Integer categoryId) {
        if (categoryId == null) {
            return;
        }

        for (CourseCategory category : categoryList.getItems()) {
            if (categoryId.equals(category.getId())) {
                categoryList.getSelectionModel().select(category);
                return;
            }
        }
    }

    private void loadCourses(CourseCategory category) {
        courseTable.getItems().clear();
        descriptionArea.clear();
        requirementTypesByCourseId.clear();

        if (category == null || category.getId() == null) {
            return;
        }

        List<Course> courses;

        try {
            courses = courseService.getCoursesByCategoryId(category.getId());
            Curriculum curriculum = curriculumComboBox.getValue();

            if (curriculum != null && !curriculum.isAllOption()) {
                for (CurriculumCourse relation : curriculumCourseService.getCurriculumCourses(curriculum.getId())) {
                    requirementTypesByCourseId.put(relation.getCourseId(), relation.getRequirementType());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("curriculum.error.courseLoad"), e);
        }

        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    @FXML
    private void addCourse() {
        Curriculum curriculum = curriculumComboBox.getSelectionModel().getSelectedItem();
        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption()) {
            showError(I18n.text("curriculum.error.curriculumTarget"));
            return;
        }

        if (category == null) {
            showError(I18n.text("curriculum.error.categoryFirst"));
            return;
        }

        Optional<CourseFormResult> courseResult = showCourseDialog(
                null,
                category,
                ELECTIVE,
                I18n.text("curriculum.course.add.title"),
                I18n.text("dialog.button.add")
        );

        if (courseResult.isEmpty()) {
            return;
        }

        try {
            CourseFormResult formResult = courseResult.get();
            Course course = formResult.course();
            courseService.addCourse(course);
            curriculumCourseService.addCourseToCurriculum(curriculum.getId(), course.getId(), formResult.requirementType());
            loadCourses(category);
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.courseAdd"), e);
        }
    }

    @FXML
    private void editCourse() {
        Curriculum curriculum = curriculumComboBox.getValue();
        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();
        Course course = courseTable.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption() || category == null || course == null) {
            showError(I18n.text("curriculum.error.courseEditSelection"));
            return;
        }

        try {
            CurriculumCourse relation = curriculumCourseService.getCurriculumCourses(curriculum.getId()).stream()
                    .filter(item -> course.getId().equals(item.getCourseId()))
                    .findFirst()
                    .orElse(null);
            String requirementType = relation == null || relation.getRequirementType() == null || relation.getRequirementType().isBlank()
                    ? ELECTIVE
                    : relation.getRequirementType();
            Optional<CourseFormResult> result = showCourseDialog(
                    course,
                    category,
                    requirementType,
                    I18n.text("curriculum.course.edit.title"),
                    I18n.text("dialog.button.save")
            );

            if (result.isEmpty()) {
                return;
            }

            CourseFormResult formResult = result.get();
            Course edited = formResult.course();
            course.setCourseCode(edited.getCourseCode());
            course.setName(edited.getName());
            course.setCredits(edited.getCredits());
            course.setDescription(edited.getDescription());
            courseService.updateCourse(course);

            if (relation == null) {
                curriculumCourseService.addCourseToCurriculum(curriculum.getId(), course.getId(), formResult.requirementType());
            } else {
                relation.setRequirementType(formResult.requirementType());
                curriculumCourseService.update(relation);
            }

            loadCourses(category);
            courseTable.getItems().stream()
                    .filter(item -> course.getId().equals(item.getId()))
                    .findFirst()
                    .ifPresent(item -> courseTable.getSelectionModel().select(item));
        } catch (Exception e) {
            showError(I18n.text("curriculum.error.courseEdit"), e);
        }
    }

    private Optional<CourseFormResult> showCourseDialog(Course course, CourseCategory category, String requirementType, String title, String saveText) {
        Dialog<CourseFormResult> dialog = AppDialogs.create(
                title,
                I18n.text("curriculum.course.dialog.header")
        );
        ButtonType saveButtonType = new ButtonType(saveText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        TextField courseCodeField = new TextField(course == null || course.getCourseCode() == null ? "" : course.getCourseCode());
        courseCodeField.setPromptText(I18n.text("curriculum.course.code.prompt"));
        TextField courseNameField = new TextField(course == null || course.getName() == null ? "" : course.getName());
        courseNameField.setPromptText(I18n.text("curriculum.course.name.prompt"));
        TextField creditsField = new TextField(course == null ? "" : String.valueOf(course.getCredits()));
        creditsField.setPromptText(I18n.text("curriculum.course.credits.prompt"));
        ComboBox<String> requirementTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(REQUIRED, ELECTIVE));
        requirementTypeComboBox.setValue(requirementType);
        TextArea memoArea = new TextArea(course == null || course.getDescription() == null ? "" : course.getDescription());
        memoArea.setPromptText(I18n.text("curriculum.course.memo.prompt"));
        memoArea.setPrefRowCount(4);
        memoArea.setWrapText(true);
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPrefWidth(460);
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(90);
        labelColumn.setPrefWidth(90);
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setMinWidth(260);
        inputColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, inputColumn);
        courseCodeField.setMaxWidth(Double.MAX_VALUE);
        courseNameField.setMaxWidth(Double.MAX_VALUE);
        creditsField.setMaxWidth(Double.MAX_VALUE);
        requirementTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        memoArea.setMaxWidth(Double.MAX_VALUE);
        form.addRow(0, new Label(I18n.text("curriculum.course.code")), courseCodeField);
        form.addRow(1, new Label(I18n.text("curriculum.course.name")), courseNameField);
        form.addRow(2, new Label(I18n.text("curriculum.course.credits")), creditsField);
        form.addRow(3, new Label(I18n.text("common.requirementType")), requirementTypeComboBox);
        form.addRow(4, new Label(I18n.text("curriculum.course.memo")), memoArea);
        dialog.getDialogPane().setContent(form);
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (courseCodeField.getText().isBlank() || courseNameField.getText().isBlank()) {
                showError(I18n.text("curriculum.error.courseFields"));
                event.consume();
                return;
            }

            try {
                double credits = Double.parseDouble(creditsField.getText().trim());

                if (!Double.isFinite(credits) || credits < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                showError(I18n.text("curriculum.error.courseCredits"));
                event.consume();
            }
        });
        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return null;
            }

            Course resultCourse = new Course(
                    courseCodeField.getText().trim(),
                    courseNameField.getText().trim(),
                    Double.parseDouble(creditsField.getText().trim()),
                    category.getId(),
                    memoArea.getText().trim());
            return new CourseFormResult(resultCourse, requirementTypeComboBox.getValue());
        });
        return dialog.showAndWait();
    }

    private record CourseFormResult(Course course, String requirementType) {
    }

    @FXML
    private void deleteCourse() {
        Course course = courseTable.getSelectionModel().getSelectedItem();

        if (course == null) {
            showError(
                    I18n.text("curriculum.course.delete.title"),
                    new Exception(I18n.text("curriculum.error.courseDeleteSelection"))
            );
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("curriculum.course.delete.title"),
                I18n.text("curriculum.course.delete.header"),
                I18n.text("curriculum.course.delete.content", course.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            CourseCategory category = categoryList.getSelectionModel().getSelectedItem();
            courseService.deleteCourse(course.getId());

            if (category != null) {
                loadCourses(category);
            } else {
                courseTable.getItems().clear();
                descriptionArea.clear();
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.course.delete.title"),
                    new Exception(I18n.text("curriculum.error.courseDelete", e.getMessage()))
            );
        }
    }

    private void showCourseDescription(Course course) {
        if (course == null) {
            descriptionArea.clear();
            return;
        }

        descriptionArea.setText(course.getDescription() == null ? "" : course.getDescription());
    }

    @FXML
    private void refresh() {
        Integer curriculumId = getId(curriculumComboBox.getSelectionModel().getSelectedItem());
        Integer categoryId = getId(categoryList.getSelectionModel().getSelectedItem());
        loadAcademicContext();
        selectCurriculum(curriculumId);
        selectCategory(categoryId);
    }

    private Integer getId(Object value) {
        if (value instanceof Department department) {
            return department.getId();
        } else if (value instanceof Major major) {
            return major.getId();
        } else if (value instanceof Curriculum curriculum) {
            return curriculum.getId();
        } else if (value instanceof CourseCategory category) {
            return category.getId();
        }

        return null;
    }

    private void selectDepartment(Integer id) {
        if (id == null) {
            return;
        }

        for (Department department : departmentComboBox.getItems()) {
            if (id.equals(department.getId())) {
                departmentComboBox.getSelectionModel().select(department);
                return;
            }
        }
    }

    private void selectMajor(Integer id) {
        if (id == null) {
            return;
        }

        for (Major major : majorComboBox.getItems()) {
            if (id.equals(major.getId())) {
                majorComboBox.getSelectionModel().select(major);
                return;
            }
        }
    }

    private void selectCurriculum(Integer id) {
        if (id == null) {
            return;
        }

        for (Curriculum curriculum : curriculumComboBox.getItems()) {
            if (id.equals(curriculum.getId())) {
                curriculumComboBox.getSelectionModel().select(curriculum);
                return;
            }
        }
    }

    private void selectCategory(Integer id) {
        if (id == null) {
            return;
        }

        for (CourseCategory category : categoryList.getItems()) {
            if (id.equals(category.getId())) {
                categoryList.getSelectionModel().select(category);
                return;
            }
        }
    }

    @FXML
    private void editMajor() {
        Major major = majorComboBox.getValue();

        if (major == null) {
            showError(
                    I18n.text("curriculum.major.edit.title"),
                    new Exception(I18n.text("curriculum.error.majorEditSelection"))
            );
            return;
        }

        TextInputDialog dialog = AppDialogs.textInput(
                major.getName(),
                I18n.text("curriculum.major.edit.title"),
                I18n.text("curriculum.major.edit.header"),
                I18n.text("curriculum.major.name")
        );
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {
            showError(
                    I18n.text("curriculum.major.edit.title"),
                    new Exception(I18n.text("curriculum.error.majorName"))
            );
            return;
        }

        try {
            Integer majorId = major.getId();
            major.setName(name);
            service.updateMajor(major);
            Department department = departmentComboBox.getValue();

            if (department != null) {
                loadMajors(department);
            }

            selectMajorById(majorId);
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.major.edit.title"),
                    new Exception(I18n.text("curriculum.error.majorUpdate", e.getMessage()))
            );
        }
    }

    @FXML
    private void deleteMajor() {
        Major major = majorComboBox.getValue();

        if (major == null) {
            showError(
                    I18n.text("curriculum.major.delete.title"),
                    new Exception(I18n.text("curriculum.error.majorDeleteSelection"))
            );
            return;
        }

        boolean confirmed = AppAlerts.confirm(
                I18n.text("curriculum.major.delete.title"),
                I18n.text("curriculum.major.delete.header"),
                I18n.text("curriculum.major.delete.content", major.getName())
        );

        if (!confirmed) {
            return;
        }

        try {
            Department department = departmentComboBox.getValue();
            service.deleteMajor(major.getId());

            if (department != null) {
                loadMajors(department);
            }
        } catch (Exception e) {
            showError(
                    I18n.text("curriculum.major.delete.title"),
                    new Exception(I18n.text("curriculum.error.majorDelete", e.getMessage()))
            );
        }
    }

    private void selectMajorById(Integer majorId) {
        if (majorId == null) {
            return;
        }

        for (Major major : majorComboBox.getItems()) {
            if (majorId.equals(major.getId())) {
                majorComboBox.setValue(major);
                return;
            }
        }
    }
}
