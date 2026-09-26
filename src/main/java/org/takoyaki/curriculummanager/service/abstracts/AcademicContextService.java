package org.takoyaki.curriculummanager.service.abstracts;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.repository.AppSettingRepository;
import org.takoyaki.curriculummanager.repository.interfaces.SettingRepository;
import org.takoyaki.curriculummanager.service.CurriculumService;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;

import java.sql.SQLException;
import java.util.List;

public class AcademicContextService implements AcademicContextProvider {
    private static final String DEPARTMENT_KEY = "current_department_id";
    private static final String MAJOR_KEY = "current_major_id";
    private final CurriculumService curriculumService;
    private final SettingRepository settingRepository;

    public AcademicContextService() {
        this(new CurriculumService(), new AppSettingRepository());
    }

    AcademicContextService(CurriculumService curriculumService, SettingRepository settingRepository) {
        this.curriculumService = curriculumService;
        this.settingRepository = settingRepository;
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
            throw new RuntimeException("既定の学部を取得できませんでした。", e);
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
    public void setCurrent(Department department, Major major) {
        if (department == null || department.getId() == null || major == null || major.getId() == null
                || !department.getId().equals(major.getDepartmentId())) {
            throw new IllegalArgumentException("同じ学部に所属する学科を選択してください。");
        }

        write(DEPARTMENT_KEY, String.valueOf(department.getId()));
        write(MAJOR_KEY, String.valueOf(major.getId()));
    }

    @Override
    public void ensureConfigured() {
        if (getCurrentDepartment() != null && getCurrentMajor() != null) {
            return;
        }

        Dialog<AcademicSelection> dialog = new Dialog<>();
        dialog.setTitle("初期設定");
        dialog.setHeaderText("所属する学部と学科を入力してください。\n次回からこの設定が自動的に使用されます。");
        ButtonType saveButtonType = new ButtonType("開始", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        TextField departmentField = new TextField();
        departmentField.setPromptText("例: 工学部");
        TextField majorField = new TextField();
        majorField.setPromptText("例: 情報工学科");
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.addRow(0, new Label("学部"), departmentField);
        form.addRow(1, new Label("学科"), majorField);
        dialog.getDialogPane().setContent(form);
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (departmentField.getText().isBlank() || majorField.getText().isBlank()) {
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == saveButtonType
                ? new AcademicSelection(departmentField.getText().trim(), majorField.getText().trim())
                : null);
        dialog.showAndWait().ifPresent(this::findOrCreateAndSelect);
    }

    private void findOrCreateAndSelect(AcademicSelection selection) {
        try {
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

            setCurrent(department, major);
        } catch (SQLException e) {
            throw new RuntimeException("初期設定を保存できませんでした。", e);
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
            throw new RuntimeException("学部・学科設定を保存できませんでした。", e);
        }
    }

    private record AcademicSelection(String departmentName, String majorName) {
    }
}
