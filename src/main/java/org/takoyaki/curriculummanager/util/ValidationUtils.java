package org.takoyaki.curriculummanager.util;

import org.takoyaki.curriculummanager.i18n.I18n;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new IllegalArgumentException(message);
        }

        return entity;
    }

    public static Integer requireId(Integer id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    public static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    public static void validateCurriculumId(Integer curriculumId) {
        validatePositiveId(
                curriculumId,
                I18n.text("validation.curriculum.idRequired"),
                I18n.text("validation.curriculum.idInvalid"));
    }

    public static void validateCourseId(Integer courseId) {
        validatePositiveId(
                courseId,
                I18n.text("validation.course.idRequired"),
                I18n.text("validation.course.idInvalid"));
    }

    public static void validatePositiveId(Integer id, String missingMessage, String invalidMessage) {
        if (id == null) {
            throw new IllegalArgumentException(missingMessage);
        }

        if (id <= 0) {
            throw new IllegalArgumentException(invalidMessage);
        }
    }

    public static void validateYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException(I18n.text("validation.year.invalid"));
        }
    }

    public static void validateSemester(String semester) {
        if (semester == null || semester.isBlank()) {
            throw new IllegalArgumentException(I18n.text("validation.semester.required"));
        }
    }
}
