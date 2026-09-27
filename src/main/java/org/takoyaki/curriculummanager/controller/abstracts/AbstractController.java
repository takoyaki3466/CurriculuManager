package org.takoyaki.curriculummanager.controller.abstracts;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.view.dialog.AppAlerts;

public abstract class AbstractController {
    protected void showError(String message) {
        AppAlerts.error(message);
    }

    protected void showError(String message, Throwable exception) {
        AppAlerts.error(message, exception);
    }

    protected void showInformation(String message) {
        AppAlerts.information(message);
    }

    protected String formatAcademicContext(Department department, Major major) {
        if (department == null || major == null) {
            return I18n.text("common.notConfigured");
        }

        return department.getName() + "　/　" + major.getName();
    }
}
