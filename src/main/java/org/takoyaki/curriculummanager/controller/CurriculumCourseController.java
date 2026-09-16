package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;

import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.CurriculumCourseDisplay;
import org.takoyaki.curriculummanager.service.CurriculumCourseService;
import org.takoyaki.curriculummanager.service.CurriculumService;

import java.sql.SQLException;
import java.util.List;

/**
 * カリキュラム科目設定画面を管理するController。
 *
 * <p>
 * 以下の階層で科目を設定します。
 * </p>
 *
 * <pre>
 *
 * 学部
 * ↓
 * カリキュラム
 * ↓
 * カテゴリ
 * ↓
 * 科目
 * ↓
 * 必修 / 選択
 * </pre>
 *
 * <p>
 * 実際のデータ操作はServiceへ委譲します。
 * Controllerでは画面状態の管理と、
 * ユーザー操作への応答を担当します。
 * </p>
 */
public class CurriculumCourseController {
    /**
     * 必修を表す文字列。
     */
    private static final String REQUIREMENT_REQUIRED = "必修";

    /**
     * 選択を表す文字列。
     */
    private static final String REQUIREMENT_ELECTIVE = "選択";

    /**
     * 学部選択ComboBox。
     */
    @FXML
    private ComboBox<Department> departmentComboBox;

    /**
     * カリキュラム選択ComboBox。
     */
    @FXML
    private ComboBox<Curriculum> curriculumComboBox;

    /**
     * カテゴリ選択ComboBox。
     */
    @FXML
    private ComboBox<CourseCategory> categoryComboBox;

    /**
     * カリキュラム科目一覧。
     */
    @FXML
    private TableView<CurriculumCourseDisplay> courseTable;

    /**
     * 科目コード列。
     */
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> courseCodeColumn;

    /**
     * 科目名列。
     */
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> courseNameColumn;

    /**
     * 単位数列。
     */
    @FXML
    private TableColumn<CurriculumCourseDisplay, Number> creditsColumn;

    /**
     * 必修 / 選択列。
     */
    @FXML
    private TableColumn<CurriculumCourseDisplay, String> requirementTypeColumn;

    /**
     * カリキュラム操作Service。
     */
    private final CurriculumService curriculumService;

    /**
     * カリキュラム科目操作Service。
     */
    private final CurriculumCourseService curriculumCourseService;

    /**
     * Controllerを生成します。
     */
    public CurriculumCourseController() {

        curriculumService = new CurriculumService();

        curriculumCourseService = new CurriculumCourseService();
    }

    /**
     * Controller初期化処理。
     */
    @FXML
    private void initialize() throws SQLException {

        setupColumns();

        setupRequirementTypeColumn();

        setupDepartmentComboBox();

        setupCurriculumComboBox();

        setupCategoryComboBox();

        loadDepartments();
    }

    /**
     * TableViewの通常の列を設定します。
     */
    private void setupColumns() {

        /*
         * 科目コード。
         */
        courseCodeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseCode()));

        /*
         * 科目名。
         */
        courseNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseName()));

        /*
         * 単位数。
         */
        creditsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getCredits()));

        /*
         * 区分の値そのものは
         * CellFactory側で表示するため、
         * CellValueFactoryも設定します。
         */
        requirementTypeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRequirementType()));
    }

    /**
     * 「必修 / 選択」列を
     * ComboBoxで編集できるように設定します。
     *
     * <p>
     * JavaFXのComboBoxTableCellを使用すると、
     * 表のセルを編集するときに
     * ComboBoxを表示できます。
     * </p>
     */
    private void setupRequirementTypeColumn() {

        /*
         * TableViewそのものを編集可能にします。
         */
        courseTable.setEditable(true);

        /*
         * 「必修」「選択」の2種類を
         * ComboBoxの選択肢にします。
         */
        requirementTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(REQUIREMENT_REQUIRED, REQUIREMENT_ELECTIVE)));

        /*
         * 編集が確定したときに呼ばれます。
         */
        requirementTypeColumn.setOnEditCommit(event -> {

            CurriculumCourseDisplay display = event.getRowValue();

            String newRequirementType = event.getNewValue();

            /*
             * nullの場合は何もしません。
             */
            if (display == null) {
                return;
            }

            if (newRequirementType == null) {
                return;
            }

            /*
             * まだカリキュラムに
             * 登録されていない科目は
             * 区分を変更できません。
             */
            if (display.getId() == null) {

                showError("この科目はまだカリキュラムに登録されていません。\n" + "先に「カリキュラムに追加」を実行してください。");

                reloadCurrentCategorySafely();

                return;
            }

            /*
             * 画面上の変更前の値を保存しておきます。
             *
             * DB更新に失敗した場合、
             * 画面を再読み込みすることで
             * DBの状態へ戻します。
             */
            String oldRequirementType = display.getRequirementType();

            try {

                /*
                 * 表示モデルの値を変更します。
                 */
                display.setRequirementType(newRequirementType);

                /*
                 * DB上のCurriculumCourseを取得します。
                 */
                CurriculumCourse curriculumCourse = new CurriculumCourse(display.getId(), display.getCurriculumId(), display.getCourseId(), newRequirementType);

                /*
                 * DBへ保存します。
                 */
                curriculumCourseService.update(curriculumCourse);

                /*
                 * TableViewへ変更を反映します。
                 */
                courseTable.refresh();

            } catch (Exception e) {

                /*
                 * DB更新に失敗した場合は
                 * 画面上の値を元へ戻します。
                 */
                display.setRequirementType(oldRequirementType);

                courseTable.refresh();

                showError("科目区分の変更に失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * 学部ComboBoxを設定します。
     *
     * <p>
     * 学部が変更された場合、
     * その学部に属するカリキュラムを
     * 読み込みます。
     * </p>
     */
    private void setupDepartmentComboBox() {

        departmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {

            try {

                loadCurricula(newValue);

            } catch (SQLException e) {

                showError("カリキュラムの取得に失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * カリキュラムComboBoxを設定します。
     *
     * <p>
     * カリキュラムが変更された場合、
     * そのカリキュラムに属するカテゴリを
     * 読み込みます。
     * </p>
     */
    private void setupCurriculumComboBox() {

        curriculumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {

            try {

                loadCategories(newValue);

            } catch (SQLException e) {

                showError("カテゴリの取得に失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * カテゴリComboBoxを設定します。
     *
     * <p>
     * カテゴリが変更された場合、
     * そのカテゴリに所属する科目を
     * 読み込みます。
     * </p>
     */
    private void setupCategoryComboBox() {

        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {

            try {

                loadCourses(newValue);

            } catch (SQLException e) {

                showError("科目の取得に失敗しました。\n" + e.getMessage());
            }
        });
    }

    /**
     * 学部一覧を読み込みます。
     */
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

    /**
     * 指定された学部のカリキュラムを
     * 読み込みます。
     *
     * @param department 学部
     */
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

    /**
     * 指定されたカリキュラムのカテゴリを
     * 読み込みます。
     *
     * @param curriculum カリキュラム
     */
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

    /**
     * 指定されたカテゴリの科目を
     * 読み込みます。
     *
     * @param category カテゴリ
     */
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

    /**
     * 選択された科目を
     * カリキュラムへ追加します。
     *
     * <p>
     * 追加時の初期値は「選択」です。
     * 追加後に表の区分列から
     * 「必修」へ変更できます。
     * </p>
     */
    @FXML
    private void addCourse() {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {

            showError("カリキュラムを選択してください。");

            return;
        }

        CurriculumCourseDisplay selectedCourse = courseTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {

            showError("科目を選択してください。");

            return;
        }

        /*
         * すでに登録されている場合は
         * 追加できません。
         */
        if (selectedCourse.getId() != null) {

            showError("この科目はすでにカリキュラムへ登録されています。");

            return;
        }

        try {

            /*
             * 新規追加時は「選択」を初期値とします。
             *
             * その後、区分列から
             * 「必修」へ変更できます。
             */
            curriculumCourseService.addCourseToCurriculum(curriculum.getId(), selectedCourse.getCourseId(), REQUIREMENT_ELECTIVE);

            reloadCurrentCategory();

        } catch (Exception e) {

            showError("科目の追加に失敗しました。\n" + e.getMessage());
        }
    }

    /**
     * 選択された科目を
     * カリキュラムから外します。
     *
     * <p>
     * 科目そのものを削除する処理ではありません。
     * </p>
     */
    @FXML
    private void removeCourse() {

        Curriculum curriculum = curriculumComboBox.getValue();

        if (curriculum == null || curriculum.getId() == null) {

            showError("カリキュラムを選択してください。");

            return;
        }

        CurriculumCourseDisplay selectedCourse = courseTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {

            showError("科目を選択してください。");

            return;
        }

        /*
         * 未登録の科目は
         * カリキュラムから外せません。
         */
        if (selectedCourse.getId() == null) {

            showError("この科目はカリキュラムに登録されていません。");

            return;
        }

        try {

            curriculumCourseService.removeCourseFromCurriculum(curriculum.getId(), selectedCourse.getCourseId());

            reloadCurrentCategory();

        } catch (Exception e) {

            showError("科目の削除に失敗しました。\n" + e.getMessage());
        }
    }

    /**
     * 現在選択されているカテゴリの
     * 科目一覧を再読み込みします。
     */
    private void reloadCurrentCategory() throws SQLException {

        loadCourses(categoryComboBox.getValue());
    }

    /**
     * 現在のカテゴリを安全に再読み込みします。
     *
     * <p>
     * 区分変更失敗時など、
     * 画面状態をDBの状態へ戻すために使用します。
     * </p>
     */
    private void reloadCurrentCategorySafely() {

        try {

            reloadCurrentCategory();

        } catch (SQLException e) {

            showError("科目一覧の再読み込みに失敗しました。\n" + e.getMessage());
        }
    }

    /**
     * カリキュラム一覧をクリアします。
     */
    private void clearCurricula() {

        curriculumComboBox.getItems().clear();
    }

    /**
     * カテゴリ一覧をクリアします。
     */
    private void clearCategories() {

        categoryComboBox.getItems().clear();
    }

    /**
     * 科目一覧をクリアします。
     */
    private void clearCourses() {

        courseTable.getItems().clear();
    }

    /**
     * エラーダイアログを表示します。
     *
     * @param message エラーメッセージ
     */
    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("エラー");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}
