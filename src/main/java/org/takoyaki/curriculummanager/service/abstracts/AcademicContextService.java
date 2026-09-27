package org.takoyaki.curriculummanager.service.abstracts;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.model.Terminology;
import org.takoyaki.curriculummanager.repository.AppSettingRepository;
import org.takoyaki.curriculummanager.repository.interfaces.SettingRepository;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.TerminologyService;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;
import org.takoyaki.curriculummanager.view.dialog.AppDialogs;

import java.sql.SQLException;
import java.util.List;

public class AcademicContextService implements AcademicContextProvider {
    private static final String DEPARTMENT_KEY = "current_department_id";
    private static final String MAJOR_KEY = "current_major_id";
    private static final String CURRICULUM_KEY = "current_curriculum_id";
    private final CurriculumService curriculumService;
    private final SettingRepository settingRepository;
    private final TerminologyService terminologyService;

    public AcademicContextService() {
        this(new CurriculumService(), new AppSettingRepository());
    }

    AcademicContextService(CurriculumService curriculumService, SettingRepository settingRepository) {
        this.curriculumService = curriculumService;
        this.settingRepository = settingRepository;
        terminologyService = new TerminologyService(settingRepository);
    }

    @Override
    public Department getCurrentDepartment() {
        Integer id = readInteger(DEPARTMENT_KEY);

        if (id == null) {
            return null;
        }

        try {
            return curriculumService.getDepartments().stream()
                    .filter(department -> id.equals(department.getId()))
                    .findFirst()
                    .orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException(I18n.text("academic.error.loadDepartment"), e);
        }
    }

    @Override
    public Major getCurrentMajor() {
        Department department = getCurrentDepartment();
        Integer id = readInteger(MAJOR_KEY);

        if (department == null || id == null) {
            return null;
        }

        Major major = curriculumService.getMajor(id);
        return major != null && department.getId().equals(major.getDepartmentId()) ? major : null;
    }

    @Override
    public Curriculum getCurrentCurriculum() {
        Major major = getCurrentMajor();
        Integer id = readInteger(CURRICULUM_KEY);

        if (major == null || id == null) {
            return null;
        }

        Curriculum allOption = Curriculum.allOption();

        if (allOption.getId().equals(id)) {
            return allOption;
        }

        Curriculum curriculum = curriculumService.getCurriculum(id);
        return curriculum != null && major.getId().equals(curriculum.getMajorId()) ? curriculum : null;
    }

    @Override
    public void setCurrent(Department department, Major major) {
        if (department == null || department.getId() == null || major == null || major.getId() == null
                || !department.getId().equals(major.getDepartmentId())) {
            throw new IllegalArgumentException(I18n.text("academic.error.invalidMajor"));
        }

        write(DEPARTMENT_KEY, String.valueOf(department.getId()));
        write(MAJOR_KEY, String.valueOf(major.getId()));
        write(CURRICULUM_KEY, String.valueOf(Curriculum.allOption().getId()));
    }

    @Override
    public void setCurrentCurriculum(Curriculum curriculum) {
        if (curriculum == null || curriculum.getId() == null) {
            throw new IllegalArgumentException(I18n.text("academic.error.curriculumRequired"));
        }

        if (!curriculum.isAllOption()) {
            Major major = getCurrentMajor();

            if (major == null || !major.getId().equals(curriculum.getMajorId())) {
                throw new IllegalArgumentException(I18n.text("academic.error.invalidCurriculum"));
            }
        }

        write(CURRICULUM_KEY, String.valueOf(curriculum.getId()));
    }

    @Override
    public void ensureConfigured() {
        Department currentDepartment = getCurrentDepartment();
        Major currentMajor = getCurrentMajor();

        if (currentDepartment != null && currentMajor != null && terminologyService.isConfigured()) {
            return;
        }

        Dialog<AcademicSelection> dialog = AppDialogs.create(
                I18n.text("dialog.initial.title"),
                I18n.text("dialog.initial.header")
        );
        ButtonType saveButtonType = new ButtonType(I18n.text("dialog.button.start"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(620);
        TextField departmentField = new TextField();
        departmentField.setPromptText(I18n.text("dialog.initial.department.prompt"));
        TextField majorField = new TextField();
        majorField.setPromptText(I18n.text("dialog.initial.major.prompt"));

        if (currentDepartment != null) {
            departmentField.setText(currentDepartment.getName());
        }

        if (currentMajor != null) {
            majorField.setText(currentMajor.getName());
        }

        Terminology currentTerminology = terminologyService.getTerminology();
        ComboBox<String> terminologyComboBox = new ComboBox<>();
        terminologyComboBox.getItems().addAll(
                Terminology.DEFAULT_CURRICULUM_NAME,
                I18n.text("terminology.category"),
                I18n.text("terminology.other")
        );
        terminologyComboBox.setPrefWidth(260);
        TextField customTerminologyField = new TextField();
        customTerminologyField.setPromptText(I18n.text("dialog.initial.customName.prompt"));
        customTerminologyField.setPrefWidth(260);
        initializeTerminologySelection(currentTerminology, terminologyComboBox, customTerminologyField);
        terminologyComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                customTerminologyField.setDisable(!I18n.text("terminology.other").equals(newValue))
        );
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.addRow(0, new Label(I18n.text("common.department")), departmentField);
        form.addRow(1, new Label(I18n.text("common.major")), majorField);
        form.addRow(2, new Label(I18n.text("dialog.initial.displayName")), terminologyComboBox);
        form.addRow(3, new Label(I18n.text("dialog.initial.customDisplayName")), customTerminologyField);
        Label curriculumDescription = createDescriptionLabel(I18n.text("dialog.initial.description"));
        Label terminologyDescription = createDescriptionLabel(I18n.text("dialog.initial.sameData"));
        VBox content = new VBox(10, curriculumDescription, terminologyDescription, form);
        dialog.getDialogPane().setContent(content);
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (departmentField.getText().isBlank()
                    || majorField.getText().isBlank()
                    || readTerminologyName(terminologyComboBox, customTerminologyField).isBlank()) {
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == saveButtonType
                ? new AcademicSelection(
                        departmentField.getText().trim(),
                        majorField.getText().trim(),
                        readTerminologyName(terminologyComboBox, customTerminologyField)
                )
                : null);
        dialog.showAndWait().ifPresent(this::findOrCreateAndSelect);
    }

    private Label createDescriptionLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(560);
        return label;
    }

    private void initializeTerminologySelection(
            Terminology terminology,
            ComboBox<String> terminologyComboBox,
            TextField customTerminologyField
    ) {
        String currentName = terminology.curriculumName();

        if (Terminology.DEFAULT_CURRICULUM_NAME.equals(currentName)
                || I18n.text("terminology.category").equals(currentName)) {
            terminologyComboBox.setValue(currentName);
            customTerminologyField.setDisable(true);
            return;
        }

        terminologyComboBox.setValue(I18n.text("terminology.other"));
        customTerminologyField.setText(currentName);
        customTerminologyField.setDisable(false);
    }

    private String readTerminologyName(
            ComboBox<String> terminologyComboBox,
            TextField customTerminologyField
    ) {
        String selection = terminologyComboBox.getValue();

        if (I18n.text("terminology.other").equals(selection)) {
            return customTerminologyField.getText().trim();
        }

        return selection == null ? "" : selection.trim();
    }

    private void findOrCreateAndSelect(AcademicSelection selection) {
        try {
            Department previousDepartment = getCurrentDepartment();
            Major previousMajor = getCurrentMajor();
            Department department = curriculumService.getDepartments().stream()
                    .filter(item -> item.getName().equals(selection.departmentName()))
                    .findFirst()
                    .orElse(null);

            if (department == null) {
                department = new Department(selection.departmentName());
                curriculumService.addDepartment(department);
            }

            List<Major> majors = curriculumService.getMajors(department.getId());
            Major major = majors.stream()
                    .filter(item -> item.getName().equals(selection.majorName()))
                    .findFirst()
                    .orElse(null);

            if (major == null) {
                major = new Major(department.getId(), selection.majorName());
                curriculumService.addMajor(major);
            }

            boolean sameSelection = previousDepartment != null
                    && previousMajor != null
                    && previousDepartment.getId().equals(department.getId())
                    && previousMajor.getId().equals(major.getId());

            if (!sameSelection) {
                setCurrent(department, major);
            }

            terminologyService.saveTerminology(selection.curriculumName());
            I18n.reloadTerminology();
        } catch (SQLException e) {
            throw new RuntimeException(I18n.text("academic.error.initialSave"), e);
        }
    }

    private Integer readInteger(String key) {
        String value;

        try {
            value = settingRepository.findValue(key);
            return value == null ? null : Integer.valueOf(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void write(String key, String value) {
        try {
            settingRepository.saveValue(key, value);
        } catch (RuntimeException e) {
            throw new RuntimeException(I18n.text("academic.error.contextSave"), e);
        }
    }

    private record AcademicSelection(
            String departmentName,
            String majorName,
            String curriculumName
    ) {
    }
}
