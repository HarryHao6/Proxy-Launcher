package com.proxylauncher.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppEntryDialogLayoutTest {
    @Test
    public void errorLabelStartsHiddenUntilValidationFails() throws IOException {
        String fxml = readDialogView();

        assertTrue(fxml.contains("fx:id=\"errorLabel\""), "The dialog should include an error label.");
        assertTrue(fxml.contains("managed=\"false\""), "The error label should not take layout space before errors exist.");
        assertTrue(fxml.contains("visible=\"false\""), "The error label should stay hidden until a validation error is shown.");
    }

    @Test
    public void compatibilityModeToggleIsNotExposedInTheDialog() throws IOException {
        String fxml = readDialogView();

        assertFalse(fxml.contains("launchViaCmdCheckBox"), "The dialog should not expose a separate compatibility-mode checkbox.");
        assertFalse(fxml.contains("Compatibility mode"), "The dialog should not mention compatibility mode after start becomes the default strategy.");
    }

    public String readDialogView() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/com/proxylauncher/ui/app-entry-dialog.fxml")) {
            if (stream == null) {
                throw new IOException("app-entry-dialog.fxml was not found on the test classpath.");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
