package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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

/**
 * 履修登録画面を管理するController。
 */
public class EnrollmentController {

    @FXML
    private ComboBox<Department> departmentComboBox;

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

    /**
     * 学部・カリキュラム・カテゴリなどを扱うサービス。
     */
    private final CurriculumService curriculumService;

    /**
     * カリキュラムと科目の関連を扱うサービス。
     */
    private final CurriculumCourseService curriculumCourseService;

    /**
     * 履修情報を扱うサービス。
     */
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

        setupDepartmentComboBox();

        setupCurriculumComboBox();

        setupCourseComboBox();

        loadDepartments();
    }

    /**
     * 履修一覧のTableViewを設定する。
     */
    private void setupColumns() {

        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));

        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));

        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("gradeSymbol"));
    }

    /**
     * 学期選択を設定する。
     */
    private void setupSemesterComboBox() {

        semesterComboBox.setItems(FXCollections.observableArrayList("前期", "後期"));

        semesterComboBox.getSelectionModel().selectFirst();
    }

    /**
     * 年度選択を設定する。
     */
    private void setupYearComboBox() {

        int currentYear = Year.now().getValue();

        List<Integer> years = java.util.stream.IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList();

        yearComboBox.setItems(FXCollections.observableArrayList(years));

        yearComboBox.setValue(currentYear);
    }

    /**
     * 学部選択変更時の処理を設定する。
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
     * カリキュラム選択変更時の処理を設定する。
     */
    private void setupCurriculumComboBox() {

        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                /*
                 * カリキュラムが変わったら、
                 * そのカリキュラムに所属する科目を
                 * 読み込む。
                 */
                loadCourses(newValue);

                /*
                 * 同じカリキュラムの履修情報だけを
                 * 読み込む。
                 */
                loadEnrollments(newValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 科目ComboBoxを初期化する。
     */
    private void setupCourseComboBox() {

        courseComboBox.setItems(FXCollections.observableArrayList());
    }

    /**
     * 学部一覧を読み込む。
     */
    private void loadDepartments() throws SQLException {

        List<Department> departments = curriculumService.getDepartments();

        departmentComboBox.setItems(FXCollections.observableArrayList(departments));

        if (!departments.isEmpty()) {

            departmentComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * 選択された学部のカリキュラムを読み込む。
     */
    private void loadCurricula(Department department) throws SQLException {

        if (department == null || department.getId() == null) {

            curriculumComboBox.getItems().clear();
            courseComboBox.getItems().clear();
            enrollmentTable.getItems().clear();

            return;
        }

        List<Curriculum> curricula = curriculumService.getCurricula(department.getId());

        var items = FXCollections.<Curriculum>observableArrayList();

        items.add(Curriculum.allOption());

        items.addAll(curricula);

        curriculumComboBox.setItems(items);

        if (!items.isEmpty()) {

            curriculumComboBox.getSelectionModel().selectFirst();

        } else {

            courseComboBox.getItems().clear();
            enrollmentTable.getItems().clear();
        }
    }

    /**
     * 選択されたカリキュラムに登録されている
     * 科目を読み込む。
     *
     * <p>
     * 以前はカテゴリから科目を検索していましたが、
     * 現在はCurriculumCourseを利用して
     * カリキュラムとの関連を直接取得する。
     * </p>
     */
    private void loadCourses(Curriculum curriculum) throws SQLException {

        courseComboBox.getItems().clear();

        if (curriculum == null || curriculum.getId() == null) {

            return;
        }

        if (curriculum.isAllOption()) {

            return;
        }

        /*
         * CurriculumCourseServiceを利用して、
         * 現在のカリキュラムに登録されている
         * 科目だけを取得する。
         */
        List<Course> courses = curriculumCourseService.getCourses(curriculum.getId());

        courseComboBox.setItems(FXCollections.observableArrayList(courses));
    }

    /**
     * 選択されたカリキュラムの履修一覧を読み込む。
     */
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

    /**
     * 履修を追加する。
     */
    @FXML
    private void addEnrollment() {

        Curriculum curriculum = curriculumComboBox.getValue();

        Course course = courseComboBox.getValue();

        Integer year = yearComboBox.getValue();

        String semester = semesterComboBox.getValue();

        if (curriculum == null || curriculum.isAllOption()) {

            showError("履修を登録するカリキュラムを選択してください。");

            return;
        }

        if (course == null) {

            showError("科目を選択してください。");

            return;
        }

        if (year == null) {

            showError("年度を選択してください。");

            return;
        }

        if (semester == null || semester.isBlank()) {

            showError("学期を選択してください。");

            return;
        }

        try {

            /*
             * 念のため、
             * 選択した科目が本当に現在の
             * カリキュラムに登録されているか確認する。
             */
            if (!curriculumCourseService.exists(curriculum.getId(), course.getId())) {

                showError("選択した科目は、このカリキュラムに登録されていません。");

                return;
            }

            /*
             * 履修情報を登録する。
             */
            enrollmentService.addEnrollment(curriculum.getId(), course.getId(), year, semester);

            /*
             * 履修一覧を更新する。
             */
            loadEnrollments(curriculum);

            /*
             * 科目選択を解除する。
             */
            courseComboBox.getSelectionModel().clearSelection();

            showInformation("履修を登録しました。");

        } catch (IllegalArgumentException | SQLException e) {

            showError(e.getMessage());
        }
    }

    /**
     * 選択した履修を削除する。
     */
    @FXML
    private void deleteEnrollment() {

        EnrollmentDisplay selected = enrollmentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showError("削除する履修を選択してください。");

            return;
        }

        try {

            enrollmentService.deleteEnrollment(selected.getEnrollmentId());

            loadEnrollments(curriculumComboBox.getValue());

        } catch (IllegalArgumentException | SQLException e) {

            showError(e.getMessage());
        }
    }

    /**
     * 履修一覧を再読み込みする。
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

    /**
     * 情報ダイアログを表示する。
     */
    private void showInformation(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("完了");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
