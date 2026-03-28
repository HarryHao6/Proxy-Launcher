package com.proxylauncher.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void launchAreaIncludesAnOpenLogFolderAction() throws IOException {
        String fxml = readMainView();
        String bottomSection = fxml.substring(fxml.indexOf("<bottom>"), fxml.indexOf("</bottom>"));

        assertTrue(bottomSection.contains("handleOpenLogFolder"), "The bottom action bar should provide an action for opening the launch log folder.");
        assertTrue(bottomSection.contains("Open Log Folder"), "The bottom action bar should show a button label for opening the log folder.");
        assertTrue(bottomSection.contains("Launch Application"), "The primary launch action should stay in the fixed bottom area.");
    }

    @Test
    public void selectedApplicationPanelDoesNotExposeALaunchModeField() throws IOException {
        String fxml = readMainView();

        assertFalse(fxml.contains("selectedLaunchMethodField"), "The main view should not expose a separate launch-method field after adopting one default start strategy.");
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
