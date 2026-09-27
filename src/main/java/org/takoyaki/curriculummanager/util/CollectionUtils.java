package org.takoyaki.curriculummanager.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> List<T> uniqueNonNull(List<T> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<T> uniqueValues = new LinkedHashSet<>();

        for (T value : values) {
            if (value != null) {
                uniqueValues.add(value);
            }
        }

        return new ArrayList<>(uniqueValues);
    }
}
