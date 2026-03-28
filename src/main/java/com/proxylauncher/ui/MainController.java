package com.proxylauncher.ui;

import com.proxylauncher.model.AppConfig;
import com.proxylauncher.model.AppEntry;
import com.proxylauncher.model.LaunchRequest;
import com.proxylauncher.model.ProxyMode;
import com.proxylauncher.service.ConfigService;
import com.proxylauncher.service.LauncherService;
import com.proxylauncher.service.ValidationService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {
    private static final String APP_STYLESHEET = "/com/proxylauncher/ui/app.css";
    private static final String STATUS_INFO = "status-info";
    private static final String STATUS_SUCCESS = "status-success";
    private static final String STATUS_WARNING = "status-warning";
    private static final String STATUS_ERROR = "status-error";
    private static final String STATE_PILL_ACTIVE = "state-pill-active";
    private static final String STATE_PILL_EMPTY = "state-pill-empty";

    @FXML
    private ListView<AppEntry> appListView;
    @FXML
    private TextField selectedNameField;
    @FXML
    private TextField selectedPathField;
    @FXML
    private TextField selectedArgumentsField;
    @FXML
    private TextArea selectedNotesArea;
    @FXML
    private TextField defaultProxyField;
    @FXML
    private TextField customProxyField;
    @FXML
    private ChoiceBox<ProxyMode> modeChoiceBox;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox statusBar;
    @FXML
    private Label appCountLabel;
    @FXML
    private Label selectionStateLabel;
    @FXML
    private Label modeDescriptionLabel;
    @FXML
    private Button editAppButton;
    @FXML
    private Button deleteAppButton;
    @FXML
    private Button launchButton;

    private final ObservableList<AppEntry> appItems = FXCollections.observableArrayList();

    private ConfigService configService;
    private LauncherService launcherService;
    private ValidationService validationService;
    private AppConfig currentConfig = new AppConfig();

    @FXML
    private void initialize() {
        appListView.setItems(appItems);
        appListView.setPlaceholder(buildPlaceholderLabel());
        appListView.setCellFactory(listView -> createAppEntryCell());
        appItems.addListener((ListChangeListener<AppEntry>) change -> updateAppCount());
        appListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            showSelectedApp(newValue);
            updateSelectionState(newValue != null);
            updateActionState();
        });

        modeChoiceBox.setItems(FXCollections.observableArrayList(ProxyMode.values()));
        modeChoiceBox.getSelectionModel().select(ProxyMode.DEFAULT);
        modeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateProxyInputState();
            updateModeDescription();
        });

        defaultProxyField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                handleDefaultProxyCommit();
            }
        });

        clearSelectedApp();
        updateAppCount();
        updateSelectionState(false);
        updateModeDescription();
        updateProxyInputState();
        updateActionState();
        setStatus("Ready.", StatusTone.INFO);
    }

    public void initializeApplication(ConfigService configService,
                                      LauncherService launcherService,
                                      ValidationService validationService) {
        this.configService = configService;
        this.launcherService = launcherService;
        this.validationService = validationService;
        loadConfig();
    }

    @FXML
    private void handleAddApp() {
        AppEntry createdEntry = showAppEntryDialog(new AppEntry(), "Add Application");
        if (createdEntry == null) {
            return;
        }

        appItems.add(createdEntry);
        appListView.getSelectionModel().select(createdEntry);
        saveAppConfiguration("Application added.");
    }

    @FXML
    private void handleEditApp() {
        AppEntry selectedApp = appListView.getSelectionModel().getSelectedItem();
        if (selectedApp == null) {
            setStatus("Select an application before editing.", StatusTone.WARNING);
            return;
        }

        AppEntry updatedEntry = showAppEntryDialog(selectedApp.copy(), "Edit Application");
        if (updatedEntry == null) {
            return;
        }

        int selectedIndex = appListView.getSelectionModel().getSelectedIndex();
        appItems.set(selectedIndex, updatedEntry);
        appListView.getSelectionModel().select(updatedEntry);
        saveAppConfiguration("Application updated.");
    }

    @FXML
    private void handleDeleteApp() {
        AppEntry selectedApp = appListView.getSelectionModel().getSelectedItem();
        if (selectedApp == null) {
            setStatus("Select an application before deleting.", StatusTone.WARNING);
            return;
        }

        if (!confirmDeletion(selectedApp)) {
            return;
        }

        int selectedIndex = appListView.getSelectionModel().getSelectedIndex();
        appItems.remove(selectedIndex);

        if (appItems.isEmpty()) {
            appListView.getSelectionModel().clearSelection();
            clearSelectedApp();
            updateSelectionState(false);
            updateActionState();
        } else {
            int nextIndex = Math.min(selectedIndex, appItems.size() - 1);
            appListView.getSelectionModel().select(nextIndex);
        }

        saveAppConfiguration("Application deleted.");
    }

    @FXML
    private void handleLaunch() {
        AppEntry selectedApp = appListView.getSelectionModel().getSelectedItem();
        if (selectedApp == null) {
            setStatus("Select an application before launching.", StatusTone.WARNING);
            return;
        }

        List<String> appErrors = validationService.validateAppEntry(selectedApp);
        if (!appErrors.isEmpty()) {
            setStatus(joinMessages(appErrors), StatusTone.ERROR);
            return;
        }

        ProxyMode mode = currentMode();
        Optional<String> defaultProxyError = validationService.validateProxy(defaultProxyField.getText());
        if (mode == ProxyMode.DEFAULT && defaultProxyError.isPresent()) {
            setStatus("Default proxy: " + defaultProxyError.get(), StatusTone.ERROR);
            return;
        }

        if (mode == ProxyMode.CUSTOM) {
            Optional<String> customProxyError = validationService.validateProxy(customProxyField.getText());
            if (customProxyError.isPresent()) {
                setStatus("Custom proxy: " + customProxyError.get(), StatusTone.ERROR);
                return;
            }
        }

        String proxyToUse = mode == ProxyMode.CUSTOM ? customProxyField.getText()
                : mode == ProxyMode.DEFAULT ? defaultProxyField.getText()
                : "";

        PersistResult persistResult = persistConfigSnapshot(false);
        if (!persistResult.saved()) {
            return;
        }
        if (!persistResult.defaultProxyStored() && mode == ProxyMode.DEFAULT) {
            setStatus("Default proxy is invalid and was not saved.", StatusTone.ERROR);
            return;
        }

        try {
            launcherService.launch(new LaunchRequest(
                    selectedApp,
                    mode,
                    trim(defaultProxyField.getText()),
                    trim(proxyToUse)
            ));
            setStatus("Started " + selectedApp + ".", StatusTone.SUCCESS);
        } catch (IOException exception) {
            setStatus("Failed to launch application: " + exception.getMessage(), StatusTone.ERROR);
        }
    }

    @FXML
    private void handleDefaultProxyCommit() {
        String proxyValue = trim(defaultProxyField.getText());
        if (proxyValue.equals(trim(currentConfig.getDefaultProxy()))) {
            return;
        }

        Optional<String> proxyError = validationService.validateProxy(proxyValue);
        if (proxyError.isPresent()) {
            setStatus("Default proxy: " + proxyError.get(), StatusTone.ERROR);
            return;
        }

        persistConfigSnapshot(true);
    }

    public void shutdown() {
        persistConfigSnapshot(false);
    }

    private void loadConfig() {
        currentConfig = configService.load();
        defaultProxyField.setText(currentConfig.getDefaultProxy());
        appItems.setAll(currentConfig.getAppEntries());

        if (!appItems.isEmpty()) {
            appListView.getSelectionModel().selectFirst();
        }

        String lastLoadWarning = configService.getLastLoadWarning();
        if (lastLoadWarning != null && !lastLoadWarning.isBlank()) {
            setStatus(lastLoadWarning, StatusTone.WARNING);
        } else {
            setStatus("Configuration loaded from " + configService.getConfigFile() + ".", StatusTone.INFO);
        }
    }

    private void showSelectedApp(AppEntry appEntry) {
        if (appEntry == null) {
            clearSelectedApp();
            return;
        }

        selectedNameField.setText(appEntry.getName());
        selectedPathField.setText(appEntry.getExecutablePath());
        selectedArgumentsField.setText(appEntry.getArguments());
        selectedNotesArea.setText(appEntry.getNotes());
    }

    private void clearSelectedApp() {
        selectedNameField.clear();
        selectedPathField.clear();
        selectedArgumentsField.clear();
        selectedNotesArea.clear();
    }

    private void updateProxyInputState() {
        ProxyMode mode = modeChoiceBox.getValue();
        boolean customEnabled = mode == ProxyMode.CUSTOM;
        customProxyField.setDisable(!customEnabled);
    }

    private void updateActionState() {
        boolean hasSelection = appListView.getSelectionModel().getSelectedItem() != null;
        editAppButton.setDisable(!hasSelection);
        deleteAppButton.setDisable(!hasSelection);
        launchButton.setDisable(!hasSelection);
    }

    private void updateModeDescription() {
        String description = switch (currentMode()) {
            case CUSTOM -> "Use the custom proxy below for this launch only.";
            case NONE -> "Launch without setting proxy variables for this process.";
            case DEFAULT -> "Use the saved default proxy for this launch.";
        };
        modeDescriptionLabel.setText(description);
    }

    private void updateAppCount() {
        int count = appItems.size();
        appCountLabel.setText(count == 1 ? "1 saved entry" : count + " saved entries");
    }

    private void updateSelectionState(boolean hasSelection) {
        selectionStateLabel.setText(hasSelection ? "Ready to launch" : "No selection");
        selectionStateLabel.getStyleClass().removeAll(STATE_PILL_ACTIVE, STATE_PILL_EMPTY);
        selectionStateLabel.getStyleClass().add(hasSelection ? STATE_PILL_ACTIVE : STATE_PILL_EMPTY);
    }

    private ListCell<AppEntry> createAppEntryCell() {
        return new ListCell<AppEntry>() {
            private final Label nameLabel = new Label();
            private final Label pathLabel = new Label();
            private final VBox container = new VBox(nameLabel, pathLabel);

            {
                nameLabel.getStyleClass().add("app-list-name");
                pathLabel.getStyleClass().add("app-list-path");
                pathLabel.setWrapText(true);
                container.getStyleClass().add("app-list-cell-content");
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(AppEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                nameLabel.setText(resolveDisplayName(item));
                pathLabel.setText(trim(item.getExecutablePath()).isEmpty()
                        ? "Executable path not configured"
                        : trim(item.getExecutablePath()));
                setGraphic(container);
            }
        };
    }

    private Label buildPlaceholderLabel() {
        Label placeholder = new Label("No applications yet. Add one to get started.");
        placeholder.getStyleClass().add("empty-state-label");
        placeholder.setWrapText(true);
        return placeholder;
    }

    private AppEntry showAppEntryDialog(AppEntry initialEntry, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/proxylauncher/ui/app-entry-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource(APP_STYLESHEET).toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Window owner = appListView.getScene() == null ? null : appListView.getScene().getWindow();
            if (owner != null) {
                dialogStage.initOwner(owner);
            }

            AppEntryDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setValidationService(validationService);
            controller.setEntry(initialEntry);

            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (!controller.isConfirmed()) {
                return null;
            }
            return controller.getSubmittedEntry();
        } catch (IOException exception) {
            setStatus("Failed to open the application editor: " + exception.getMessage(), StatusTone.ERROR);
            return null;
        }
    }

    private boolean confirmDeletion(AppEntry appEntry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Application");
        alert.setHeaderText("Delete " + appEntry + "?");
        alert.setContentText("This removes the entry from Proxy Launcher.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().getStylesheets().add(getClass().getResource(APP_STYLESHEET).toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-shell");

        Window owner = appListView.getScene() == null ? null : appListView.getScene().getWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void saveAppConfiguration(String successMessage) {
        PersistResult persistResult = persistConfigSnapshot(false);
        if (!persistResult.saved()) {
            return;
        }
        if (persistResult.defaultProxyStored()) {
            setStatus(successMessage, StatusTone.SUCCESS);
            return;
        }
        setStatus(successMessage + " Default proxy was not saved because the format is invalid.", StatusTone.WARNING);
    }

    private PersistResult persistConfigSnapshot(boolean showSuccessStatus) {
        boolean defaultProxyStored = tryStoreDefaultProxyFromField();
        currentConfig.setAppEntries(List.copyOf(appItems));

        try {
            configService.save(currentConfig);
        } catch (IllegalStateException exception) {
            setStatus("Failed to save configuration: " + exception.getMessage(), StatusTone.ERROR);
            return PersistResult.failed(defaultProxyStored);
        }

        if (showSuccessStatus) {
            if (defaultProxyStored) {
                setStatus("Configuration saved.", StatusTone.SUCCESS);
            } else {
                setStatus("Configuration saved, but the default proxy value is invalid and was not stored.",
                        StatusTone.WARNING);
            }
        }

        return PersistResult.saved(defaultProxyStored);
    }

    private boolean tryStoreDefaultProxyFromField() {
        Optional<String> proxyError = validationService.validateProxy(defaultProxyField.getText());
        if (proxyError.isPresent()) {
            return false;
        }
        currentConfig.setDefaultProxy(trim(defaultProxyField.getText()));
        return true;
    }

    private void setStatus(String message, StatusTone tone) {
        statusLabel.setText(message == null ? "" : message);
        ObservableList<String> styleClasses = statusBar.getStyleClass();
        styleClasses.removeAll(STATUS_INFO, STATUS_SUCCESS, STATUS_WARNING, STATUS_ERROR);
        styleClasses.add(tone.getStyleClass());
    }

    private String resolveDisplayName(AppEntry appEntry) {
        String name = trim(appEntry.getName());
        if (!name.isBlank()) {
            return name;
        }
        String path = trim(appEntry.getExecutablePath());
        return path.isBlank() ? "Unnamed application" : path;
    }

    private String joinMessages(List<String> messages) {
        return String.join(System.lineSeparator(), messages);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private ProxyMode currentMode() {
        return Objects.requireNonNullElse(modeChoiceBox.getValue(), ProxyMode.DEFAULT);
    }

    private enum StatusTone {
        INFO(STATUS_INFO),
        SUCCESS(STATUS_SUCCESS),
        WARNING(STATUS_WARNING),
        ERROR(STATUS_ERROR);

        private final String styleClass;

        StatusTone(String styleClass) {
            this.styleClass = styleClass;
        }

        private String getStyleClass() {
            return styleClass;
        }
    }

    private record PersistResult(boolean saved, boolean defaultProxyStored) {
        private static PersistResult saved(boolean defaultProxyStored) {
            return new PersistResult(true, defaultProxyStored);
        }

        private static PersistResult failed(boolean defaultProxyStored) {
            return new PersistResult(false, defaultProxyStored);
        }

    }
}
