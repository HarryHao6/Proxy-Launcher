package com.proxylauncher;

import com.proxylauncher.service.ConfigService;
import com.proxylauncher.service.LauncherService;
import com.proxylauncher.service.ValidationService;
import com.proxylauncher.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    private static final String APP_STYLESHEET = "/com/proxylauncher/ui/app.css";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/proxylauncher/ui/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1060, 720);
        scene.getStylesheets().add(MainApp.class.getResource(APP_STYLESHEET).toExternalForm());

        MainController controller = loader.getController();
        controller.initializeApplication(
                new ConfigService(),
                new LauncherService(),
                new ValidationService()
        );

        stage.setTitle("Proxy Launcher");
        stage.setMinWidth(940);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> controller.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
