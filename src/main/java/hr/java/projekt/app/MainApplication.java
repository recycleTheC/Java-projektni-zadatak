package hr.java.projekt.app;

import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        Path changesPaths = Path.of(ChangeHistoryRecordFiles.DIRECTORY);
        if (!Files.exists(changesPaths)) {
            Files.createDirectory(changesPaths);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Knjigovodstvo - (c) Mario Kopjar 2022.");
        stage.setScene(scene);
        stage.show();
    }
}