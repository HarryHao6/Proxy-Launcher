package com.proxylauncher.ui;

import com.proxylauncher.model.AppEntry;
import com.proxylauncher.service.ValidationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AppEntryDialogController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField executablePathField;
    @FXML
    private TextField argumentsField;
    @FXML
    private TextArea notesArea;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;

    private Stage dialogStage;
    private ValidationService validationService;
    private boolean confirmed;
    private AppEntry submittedEntry;

    @FXML
    private void initialize() {
        clearError();
        saveButton.setDefaultButton(true);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> clearError());
        executablePathField.textProperty().addListener((observable, oldValue, newValue) -> clearError());
        argumentsField.textProperty().addListener((observable, oldValue, newValue) -> clearError());
        notesArea.textProperty().addListener((observable, oldValue, newValue) -> clearError());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public void setEntry(AppEntry appEntry) {
        if (appEntry == null) {
            return;
        }
        nameField.setText(appEntry.getName());
        executablePathField.setText(appEntry.getExecutablePath());
        argumentsField.setText(appEntry.getArguments());
        notesArea.setText(appEntry.getNotes());
    }

    public AppEntry buildEntry() {
        return new AppEntry(
                trim(nameField.getText()),
                trim(executablePathField.getText()),
                trim(argumentsField.getText()),
                trim(notesArea.getText())
        );
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public AppEntry getSubmittedEntry() {
        return submittedEntry == null ? null : submittedEntry.copy();
    }

    @FXML
    private void handleSave() {
        AppEntry candidate = buildEntry();
        if (validationService != null) {
            List<String> errors = validationService.validateAppEntry(candidate);
            if (!errors.isEmpty()) {
                showError(joinMessages(errors));
                confirmed = false;
                return;
            }
        }

        clearError();
        submittedEntry = candidate;
        confirmed = true;
        close();
    }

    @FXML
    private void handleBrowseExecutable() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Executable");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.exe", "*.bat", "*.cmd"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        configureInitialDirectory(chooser);

        Window owner = dialogStage;
        File selectedFile = chooser.showOpenDialog(owner);
        if (selectedFile != null) {
            executablePathField.setText(selectedFile.getAbsolutePath());
            clearError();
        }
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        close();
    }

    private void close() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void clearError() {
        showError("");
    }

    private void showError(String message) {
        boolean hasError = message != null && !message.isBlank();
        errorLabel.setText(hasError ? message : "");
        errorLabel.setManaged(hasError);
        errorLabel.setVisible(hasError);
    }

    private String joinMessages(List<String> messages) {
        return String.join(System.lineSeparator(), messages);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void configureInitialDirectory(FileChooser chooser) {
        String currentPath = trim(executablePathField.getText());
        if (currentPath.isEmpty()) {
            return;
        }

        Path path = Path.of(currentPath);
        Path candidateDirectory = Files.isDirectory(path) ? path : path.getParent();
        if (candidateDirectory != null && Files.isDirectory(candidateDirectory)) {
            chooser.setInitialDirectory(candidateDirectory.toFile());
        }
    }
}
