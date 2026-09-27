package org.takoyaki.curriculummanager.util;

public final class NumberFormatUtils {
    private NumberFormatUtils() {
    }

    public static String formatCredits(double credits) {
        if (credits == Math.floor(credits)) {
            return String.valueOf((int) credits);
        }

        return String.valueOf(credits);
    }
}
