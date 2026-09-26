package org.takoyaki.curriculummanager.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
import javafx.scene.layout.GridPane;

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

import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * カリキュラム画面のController。
 *
 * <p>
 * 学部 → 学科 → カリキュラム → カテゴリ → 科目
 * の階層を管理します。
 * </p>
 */
public class CurriculumController {
    /*
     * ==============================
     * FXMLコンポーネント
     * ==============================
     */

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

    /*
     * ==============================
     * Service
     * ==============================
     */

    /**
     * 学部Service。
     */
    private final DepartmentService departmentService;

    /**
     * カリキュラム関連Service。
     */
    private final CurriculumService service;

    /**
     * カテゴリー関連Service。
     */
    private final CourseCategoryService courseCategoryService;

    /**
     * 科目関連Service。
     */
    private final CourseService courseService;

    /**
     * カリキュラムと科目の関連を管理するService。
     */
    private final CurriculumCourseService curriculumCourseService;

    private final Map<Integer, String> requirementTypesByCourseId = new HashMap<>();

    /**
     * Controllerを生成します。
     */
    public CurriculumController() {

        departmentService = new DepartmentService();

        service = new CurriculumService();

        courseCategoryService = new CourseCategoryService();

        courseService = new CourseService();

        curriculumCourseService = new CurriculumCourseService();
    }

    /**
     * 初期化処理。
     */
    @FXML
    public void initialize() {

        setupCourseTable();

        setupSelectionListeners();

        loadDepartments();
    }

    /**
     * 科目TableViewを設定します。
     */
    private void setupCourseTable() {

        courseCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));

        courseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        creditsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCredits())));

        requirementTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(requirementTypesByCourseId.getOrDefault(cellData.getValue().getId(), "")));

        courseTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showCourseDescription(newValue));
    }

    /**
     * ComboBoxやListViewの選択変更を監視します。
     */
    private void setupSelectionListeners() {

        /*
         * 学部が変更されたら、
         * その学部に所属する学科を読み込みます。
         */
        departmentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadMajors(newValue));

        /*
         * 学科が変更されたら、
         * その学科に所属するカリキュラムを読み込みます。
         */
        majorComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadCurricula(newValue));

        /*
         * カリキュラムが変更されたら、
         * カテゴリを読み込みます。
         */
        curriculumComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadCategories(newValue));

        /*
         * カテゴリが変更されたら、
         * そのカテゴリに所属する科目を読み込みます。
         */
        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadCourses(newValue));
    }

    /**
     * 学部一覧を読み込みます。
     */
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

    /**
     * 学部を追加します。
     */
    @FXML
    private void addDepartment() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("学部追加");
        dialog.setHeaderText("新しい学部を追加します。");
        dialog.setContentText("学部名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("学部名を入力してください。");

            return;
        }

        try {

            Department department = new Department(name);

            departmentService.addDepartment(department);

            loadDepartments();

        } catch (Exception e) {

            showError("学部の追加に失敗しました。", e);
        }
    }

    /**
     * 選択中の学部を編集します。
     */
    @FXML
    private void editDepartment() {

        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {

            showError("編集する学部を選択してください。");

            return;
        }

        TextInputDialog dialog = new TextInputDialog(department.getName());

        dialog.setTitle("学部編集");
        dialog.setHeaderText("学部名を変更します。");
        dialog.setContentText("学部名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("学部名を入力してください。");

            return;
        }

        try {

            department.setName(name);

            departmentService.updateDepartment(department);

            Integer departmentId = department.getId();

            loadDepartments();

            selectDepartment(departmentId);

        } catch (Exception e) {

            showError("学部の更新に失敗しました。", e);
        }
    }

    /**
     * 選択中の学部を削除します。
     */
    @FXML
    private void deleteDepartment() {

        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {

            showError("削除する学部を選択してください。");

            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("学部削除");

        alert.setHeaderText("学部を削除します。");

        alert.setContentText("「" + department.getName() + "」を削除しますか？\n\n" + "この学部に所属する学科・" + "カリキュラム・" + "カテゴリなども削除されます。\n" + "この操作は元に戻せません。");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {

            return;
        }

        try {

            departmentService.deleteDepartment(department.getId());

            loadDepartments();

        } catch (Exception e) {

            showError("学部の削除に失敗しました。", e);
        }
    }

    /**
     * 学科一覧を読み込みます。
     *
     * @param department 選択された学部
     */
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

    /**
     * 学科を追加します。
     */
    @FXML
    private void addMajor() {

        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (department == null) {

            showError("先に学部を選択してください。");

            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("学科追加");

        dialog.setHeaderText("「" + department.getName() + "」に学科を追加します。");

        dialog.setContentText("学科名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("学科名を入力してください。");

            return;
        }

        try {

            Major major = new Major(department.getId(), name);

            service.addMajor(major);

            loadMajors(department);

        } catch (Exception e) {

            showError("学科の追加に失敗しました。", e);
        }
    }

    /**
     * カリキュラム一覧を読み込みます。
     *
     * @param major 選択された学科
     */
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

        curriculumComboBox.getSelectionModel().selectFirst();
    }

    /**
     * カリキュラムを追加します。
     */
    @FXML
    private void addCurriculum() {

        Major major = majorComboBox.getSelectionModel().getSelectedItem();

        if (major == null) {

            showError("先に学科を選択してください。");

            return;
        }

        TextInputDialog nameDialog = new TextInputDialog();

        nameDialog.setTitle("カリキュラム追加");

        nameDialog.setHeaderText("新しいカリキュラムを追加します。");

        nameDialog.setContentText("カリキュラム名:");

        Optional<String> nameResult = nameDialog.showAndWait();

        if (nameResult.isEmpty()) {
            return;
        }

        String name = nameResult.get().trim();

        if (name.isBlank()) {

            showError("カリキュラム名を入力してください。");

            return;
        }

        TextInputDialog yearDialog = new TextInputDialog(String.valueOf(java.time.Year.now().getValue()));

        yearDialog.setTitle("カリキュラム追加");

        yearDialog.setHeaderText("開始年度を入力します。");

        yearDialog.setContentText("開始年度:");

        Optional<String> yearResult = yearDialog.showAndWait();

        if (yearResult.isEmpty()) {
            return;
        }

        int startYear;

        try {

            startYear = Integer.parseInt(yearResult.get().trim());

        } catch (NumberFormatException e) {

            showError("開始年度には数字を入力してください。");

            return;
        }

        try {

            Curriculum curriculum = new Curriculum(major.getId(), name, startYear);

            service.addCurriculum(curriculum);

            loadCurricula(major);

        } catch (Exception e) {

            showError("カリキュラムの追加に失敗しました。", e);
        }
    }

    /**
     * 選択中のカリキュラムを編集します。
     */
    @FXML
    private void editCurriculum() {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {

            showError("カリキュラム編集", new Exception("編集するカリキュラムを選択してください。"));

            return;
        }

        TextInputDialog dialog = new TextInputDialog(curriculum.getName());

        dialog.setTitle("カリキュラム編集");

        dialog.setHeaderText("カリキュラム名を変更します。");

        dialog.setContentText("カリキュラム名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("カリキュラム編集", new Exception("カリキュラム名を入力してください。"));

            return;
        }

        try {

            Integer curriculumId = curriculum.getId();

            curriculum.setName(name);

            service.updateCurriculum(curriculum);

            Major major = majorComboBox.getValue();

            if (major != null) {

                loadCurricula(major);

                selectCurriculumById(curriculumId);
            }

        } catch (Exception e) {

            showError("カリキュラム編集", new Exception("カリキュラムの更新に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 選択中のカリキュラムを削除します。
     */
    @FXML
    private void deleteCurriculum() {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {

            showError("カリキュラム削除", new Exception("削除するカリキュラムを選択してください。"));

            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("カリキュラム削除");

        alert.setHeaderText("カリキュラムを削除します。");

        alert.setContentText("「" + curriculum.getName() + "」を削除しますか？\n\n" + "このカリキュラムに所属する" + "カテゴリ・卒業要件・" + "カリキュラムと科目の関連も" + "削除されます。\n\n" + "科目そのものは削除されません。\n\n" + "この操作は元に戻せません。");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {

            return;
        }

        try {

            Major major = majorComboBox.getValue();

            service.deleteCurriculum(curriculum.getId());

            if (major != null) {

                loadCurricula(major);
            }

        } catch (Exception e) {

            showError("カリキュラム削除", new Exception("カリキュラムの削除に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 指定されたIDのカリキュラムを
     * ComboBoxから選択します。
     *
     * @param curriculumId 選択するカリキュラムID
     */
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

    /**
     * カテゴリ一覧を読み込みます。
     *
     * @param curriculum 選択されたカリキュラム
     */
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

                Major major = majorComboBox.getValue();

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

            throw new RuntimeException("カテゴリーの取得に失敗しました。", e);
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

    /**
     * カテゴリを追加します。
     */
    @FXML
    private void addCategory() {

        Curriculum curriculum = curriculumComboBox.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption()) {

            showError("追加先のカリキュラムを選択してください。");

            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("カテゴリ追加");

        dialog.setHeaderText("カテゴリを追加します。");

        dialog.setContentText("カテゴリ名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("カテゴリ名を入力してください。");

            return;
        }

        try {

            CourseCategory category = new CourseCategory(curriculum.getId(), null, name, 0);

            courseCategoryService.addCategory(category);

            loadCategories(curriculum);

        } catch (Exception e) {

            showError("カテゴリの追加に失敗しました。", e);
        }
    }

    /**
     * 選択中の科目カテゴリを編集します。
     */
    @FXML
    private void editCategory() {

        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (category == null) {

            showError("カテゴリ編集", new Exception("編集するカテゴリを選択してください。"));

            return;
        }

        TextInputDialog dialog = new TextInputDialog(category.getName());

        dialog.setTitle("カテゴリ編集");

        dialog.setHeaderText("カテゴリ名を変更します。");

        dialog.setContentText("カテゴリ名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("カテゴリ編集", new Exception("カテゴリ名を入力してください。"));

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

            showError("カテゴリ編集", new Exception("カテゴリの更新に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 選択中の科目カテゴリを削除します。
     */
    @FXML
    private void deleteCategory() {

        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (category == null) {

            showError("カテゴリ削除", new Exception("削除するカテゴリを選択してください。"));

            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("カテゴリ削除");

        alert.setHeaderText("カテゴリを削除します。");

        alert.setContentText("「" + category.getName() + "」を削除しますか？\n\n" + "このカテゴリに所属している" + "科目との関連も削除されます。\n" + "科目そのものは削除されません。\n\n" + "この操作は元に戻せません。");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {

            return;
        }

        try {

            Curriculum curriculum = curriculumComboBox.getValue();

            courseCategoryService.deleteCategory(category.getId());

            if (curriculum != null) {

                loadCategories(curriculum);
            }

        } catch (Exception e) {

            showError("カテゴリ削除", new Exception("カテゴリの削除に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 指定されたIDのカテゴリを
     * ListViewから選択します。
     *
     * @param categoryId 選択するカテゴリID
     */
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

    /**
     * 科目一覧を読み込みます。
     *
     * @param category 選択されたカテゴリ
     */
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

            throw new RuntimeException("科目の取得に失敗しました。", e);
        }

        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    /**
     * 科目を追加します。
     */
    @FXML
    private void addCourse() {

        Curriculum curriculum = curriculumComboBox.getSelectionModel().getSelectedItem();

        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption()) {

            showError("追加先のカリキュラムを選択してください。");

            return;
        }

        if (category == null) {

            showError("先にカテゴリを選択してください。");

            return;
        }

        Optional<CourseFormResult> courseResult = showCourseDialog(null, category, "選択", "科目追加", "追加");

        if (courseResult.isEmpty()) {

            return;
        }

        try {

            CourseFormResult formResult = courseResult.get();

            Course course = formResult.course();

            courseService.addCourse(course);

            /*
             * 作成した科目を
             * 現在のカリキュラムへ関連付けます。
             */
            curriculumCourseService.addCourseToCurriculum(curriculum.getId(), course.getId(), formResult.requirementType());

            loadCourses(category);

        } catch (Exception e) {

            showError("科目の追加に失敗しました。", e);
        }
    }

    /**
     * 選択中の科目と必修・選択区分を編集します。
     */
    @FXML
    private void editCourse() {

        Curriculum curriculum = curriculumComboBox.getValue();

        CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

        Course course = courseTable.getSelectionModel().getSelectedItem();

        if (curriculum == null || curriculum.isAllOption() || category == null || course == null) {

            showError("編集する科目を選択してください。");

            return;
        }

        try {

            CurriculumCourse relation = curriculumCourseService.getCurriculumCourses(curriculum.getId()).stream()

                    .filter(item -> course.getId().equals(item.getCourseId()))

                    .findFirst()

                    .orElse(null);

            String requirementType = relation == null || relation.getRequirementType() == null || relation.getRequirementType().isBlank()

                    ? "選択"

                    : relation.getRequirementType();

            Optional<CourseFormResult> result = showCourseDialog(course, category, requirementType, "科目編集", "保存");

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

            showError("科目の編集に失敗しました。", e);
        }
    }

    private Optional<CourseFormResult> showCourseDialog(Course course, CourseCategory category, String requirementType, String title, String saveText) {

        Dialog<CourseFormResult> dialog = new Dialog<>();

        dialog.setTitle(title);

        dialog.setHeaderText("科目の情報と必修・選択区分をまとめて入力してください。");

        ButtonType saveButtonType = new ButtonType(saveText, ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField courseCodeField = new TextField(course == null || course.getCourseCode() == null ? "" : course.getCourseCode());

        courseCodeField.setPromptText("例: CS101");

        TextField courseNameField = new TextField(course == null || course.getName() == null ? "" : course.getName());

        courseNameField.setPromptText("例: プログラミング基礎");

        TextField creditsField = new TextField(course == null ? "" : String.valueOf(course.getCredits()));

        creditsField.setPromptText("例: 2");

        ComboBox<String> requirementTypeComboBox = new ComboBox<>(FXCollections.observableArrayList("必修", "選択"));

        requirementTypeComboBox.setValue(requirementType);

        TextArea memoArea = new TextArea(course == null || course.getDescription() == null ? "" : course.getDescription());

        memoArea.setPromptText("補足事項（任意）");

        memoArea.setPrefRowCount(4);

        memoArea.setWrapText(true);

        GridPane form = new GridPane();

        form.setHgap(12);

        form.setVgap(10);

        form.setPrefWidth(460);

        form.addRow(0, new Label("授業コード"), courseCodeField);

        form.addRow(1, new Label("授業名"), courseNameField);

        form.addRow(2, new Label("単位数"), creditsField);

        form.addRow(3, new Label("区分"), requirementTypeComboBox);

        form.addRow(4, new Label("メモ"), memoArea);

        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);

        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            if (courseCodeField.getText().isBlank() || courseNameField.getText().isBlank()) {

                showError("授業コードと授業名を入力してください。");

                event.consume();

                return;
            }

            try {

                double credits = Double.parseDouble(creditsField.getText().trim());

                if (!Double.isFinite(credits) || credits < 0) {

                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {

                showError("単位数は0以上の数値で入力してください。");

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

    /**
     * 選択中の科目を削除します。
     */
    @FXML
    private void deleteCourse() {

        Course course = courseTable.getSelectionModel().getSelectedItem();

        if (course == null) {

            showError("科目削除", new Exception("削除する科目を選択してください。"));

            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("科目削除");

        alert.setHeaderText("科目を削除します。");

        alert.setContentText("「" + course.getName() + "」を削除しますか？\n\n" + "この科目を削除すると、" + "この科目に関連付けられている" + "カリキュラムとの関連や" + "履修情報も削除されます。\n\n" + "この操作は元に戻せません。");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {

            return;
        }

        try {

            /*
             * 削除後に現在のカテゴリを
             * 再読み込みするため保持します。
             */
            CourseCategory category = categoryList.getSelectionModel().getSelectedItem();

            courseService.deleteCourse(course.getId());

            /*
             * 現在のカテゴリが残っている場合は
             * 科目一覧を再読み込みします。
             */
            if (category != null) {

                loadCourses(category);

            } else {

                courseTable.getItems().clear();
                descriptionArea.clear();
            }

        } catch (Exception e) {

            showError("科目削除", new Exception("科目の削除に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 選択された科目の説明を表示します。
     *
     * @param course 選択された科目
     */
    private void showCourseDescription(Course course) {

        if (course == null) {

            descriptionArea.clear();

            return;
        }

        descriptionArea.setText(course.getDescription() == null ? "" : course.getDescription());
    }

    /**
     * エラーダイアログを表示します。
     *
     * @param message メッセージ
     */
    private void showError(String message) {

        showError(message, null);
    }

    /**
     * エラーダイアログを表示します。
     *
     * @param message   メッセージ
     * @param exception 発生した例外
     */
    private void showError(String message, Exception exception) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("エラー");

        alert.setHeaderText(message);

        if (exception != null) {

            alert.setContentText(exception.getMessage());
        }

        alert.showAndWait();
    }

    /**
     * 画面を更新します。
     */
    @FXML
    private void refresh() {

        Integer departmentId = getId(departmentComboBox.getSelectionModel().getSelectedItem());

        Integer majorId = getId(majorComboBox.getSelectionModel().getSelectedItem());

        Integer curriculumId = getId(curriculumComboBox.getSelectionModel().getSelectedItem());

        Integer categoryId = getId(categoryList.getSelectionModel().getSelectedItem());

        loadDepartments();

        selectDepartment(departmentId);

        selectMajor(majorId);

        selectCurriculum(curriculumId);

        selectCategory(categoryId);
    }

    /**
     * オブジェクトからIDを取得します。
     *
     * @param value 対象
     * @return ID
     */
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

    /**
     * 指定した学部を選択します。
     *
     * @param id 学部ID
     */
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

    /**
     * 指定した学科を選択します。
     *
     * @param id 学科ID
     */
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

    /**
     * 指定したカリキュラムを選択します。
     *
     * @param id カリキュラムID
     */
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

    /**
     * 指定したカテゴリを選択します。
     *
     * @param id カテゴリID
     */
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

    /**
     * 選択中の学科を編集します。
     */
    @FXML
    private void editMajor() {

        Major major = majorComboBox.getValue();

        if (major == null) {

            showError("学科編集", new Exception("編集する学科を選択してください。"));

            return;
        }

        TextInputDialog dialog = new TextInputDialog(major.getName());

        dialog.setTitle("学科編集");

        dialog.setHeaderText("学科名を変更します。");

        dialog.setContentText("学科名:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();

        if (name.isBlank()) {

            showError("学科編集", new Exception("学科名を入力してください。"));

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

            showError("学科編集", new Exception("学科の更新に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 選択中の学科を削除します。
     */
    @FXML
    private void deleteMajor() {

        Major major = majorComboBox.getValue();

        if (major == null) {

            showError("学科削除", new Exception("削除する学科を選択してください。"));

            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("学科削除");

        alert.setHeaderText("学科を削除します。");

        alert.setContentText("「" + major.getName() + "」を削除しますか？\n\n" + "この学科に所属するカリキュラム・" + "カテゴリ・カリキュラムと科目の関連も" + "削除されます。\n" + "科目そのものは残りますが、" + "カテゴリとの関連が外れる場合があります。\n\n" + "この操作は元に戻せません。");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {

            return;
        }

        try {

            Department department = departmentComboBox.getValue();

            service.deleteMajor(major.getId());

            if (department != null) {

                loadMajors(department);
            }

        } catch (Exception e) {

            showError("学科削除", new Exception("学科の削除に失敗しました。\n" + e.getMessage()));
        }
    }

    /**
     * 指定されたIDの学科を
     * ComboBoxから選択します。
     *
     * @param majorId 選択する学科ID
     */
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
