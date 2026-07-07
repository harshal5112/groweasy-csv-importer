package com.groweasy.csvimporter.util;

import java.util.ArrayList;
import java.util.List;

public final class BatchUtils {

    private BatchUtils() {}

    public static <T> List<List<T>> partition(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        if (items == null || items.isEmpty()) return batches;
        int size = Math.max(1, batchSize);
        for (int i = 0; i < items.size(); i += size) {
            batches.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return batches;
    }
}
