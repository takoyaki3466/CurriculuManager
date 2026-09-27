package org.takoyaki.curriculummanager.util;

import org.takoyaki.curriculummanager.i18n.I18n;

public final class RequirementTypeUtils {
    public static final String REQUIRED = I18n.raw("requirementType.required");
    public static final String ELECTIVE = I18n.raw("requirementType.elective");

    private RequirementTypeUtils() {
    }

    public static boolean isRequired(String requirementType) {
        return requirementType != null && REQUIRED.equals(requirementType.trim());
    }
}
