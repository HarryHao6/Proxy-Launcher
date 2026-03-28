package com.proxylauncher.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainViewLayoutTest {
    @Test
    public void applicationActionButtonsAppearBeforeTheListViewInTheLeftPanel() throws IOException {
        String fxml = readMainView();
        int addButtonIndex = fxml.indexOf("text=\"Add App\"");
        int listViewIndex = fxml.indexOf("<ListView fx:id=\"appListView\"");

        assertTrue(addButtonIndex >= 0, "The Add App button should exist in the main view.");
        assertTrue(listViewIndex >= 0, "The application list view should exist in the main view.");
        assertTrue(
                addButtonIndex < listViewIndex,
                "Add/Edit/Delete actions should appear before the scrollable list so they stay reachable on shorter windows."
        );
    }

    public String readMainView() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/com/proxylauncher/ui/main-view.fxml")) {
            if (stream == null) {
                throw new IOException("main-view.fxml was not found on the test classpath.");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
