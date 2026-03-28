package com.proxylauncher.util;

import java.util.ArrayList;
import java.util.List;

public final class ListReorder {
    private ListReorder() {
    }

    public static <T> List<T> move(List<T> items, int fromIndex, int toIndex) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        if (fromIndex < 0 || fromIndex >= items.size() || toIndex < 0 || toIndex >= items.size()) {
            throw new IndexOutOfBoundsException("List reorder indices are out of bounds.");
        }
        if (fromIndex == toIndex) {
            return List.copyOf(items);
        }

        List<T> reordered = new ArrayList<>(items);
        T movedItem = reordered.remove(fromIndex);
        reordered.add(toIndex, movedItem);
        return List.copyOf(reordered);
    }
}
