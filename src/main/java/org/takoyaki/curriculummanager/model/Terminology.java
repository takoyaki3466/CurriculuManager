package org.takoyaki.curriculummanager.model;

import org.takoyaki.curriculummanager.i18n.I18n;

public record Terminology(String curriculumName) {
    public static final String DEFAULT_CURRICULUM_NAME = I18n.raw("common.curriculum");

    public Terminology {
        curriculumName = normalize(curriculumName, DEFAULT_CURRICULUM_NAME);
    }

    private static String normalize(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
