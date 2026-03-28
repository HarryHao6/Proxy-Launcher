package com.proxylauncher.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListReorderTest {
    @Test
    public void movePlacesTheDraggedItemAtTheRequestedIndex() {
        List<String> reordered = ListReorder.move(List.of("IDEA", "Codex", "Notepad"), 0, 2);

        assertEquals(List.of("Codex", "Notepad", "IDEA"), reordered);
    }

    @Test
    public void moveLeavesTheListUntouchedWhenTheIndexDoesNotChange() {
        List<String> reordered = ListReorder.move(List.of("IDEA", "Codex", "Notepad"), 1, 1);

        assertEquals(List.of("IDEA", "Codex", "Notepad"), reordered);
    }
}
