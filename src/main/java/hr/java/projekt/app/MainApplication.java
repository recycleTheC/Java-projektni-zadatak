package hr.java.projekt.app;

import hr.java.projekt.controller.LoginController;
import hr.java.projekt.model.account.AccountParameters;
import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import hr.java.projekt.model.operator.Operator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class MainApplication extends Application {
    private final static String PARAMETERS_URL = "files/parameters.properties";
    private static final Map<AccountParameters, String> accountParameters = new HashMap<>();
    public static boolean historyWritingInProgress = false, refreshHomeScreen = true;
    public static Operator operator = null;
    public static Optional<String> lastHistoryRecord = Optional.empty();
    private static Stage mainStage;

    public static void main(String[] args) {
        launch();
    }

    public static Optional<String> getAccountParameter(AccountParameters parameter) {
        return Optional.ofNullable(accountParameters.get(parameter));
    }

    public static void loadParameters() throws IOException {
        Path parametersPath = Path.of(PARAMETERS_URL);
        if (!Files.exists(parametersPath)) {
            Files.createFile(parametersPath);
        }

        Properties parameters = new Properties();
        parameters.load(new FileInputStream(parametersPath.toFile()));

        accountParameters.clear();

        for (AccountParameters parameter : AccountParameters.values()) {
            String account = parameters.getProperty(parameter.name());
            accountParameters.put(parameter, account);
        }
    }

    public static void startMain() throws IOException {
        loadParameters();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        mainStage.setTitle("Knjigovodstvo (operater: " + operator.getName() + ") - (c) Mario Kopjar 2022.");
        mainStage.setScene(scene);
        mainStage.show();
    }

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        Path changesPaths = Path.of(ChangeHistoryRecordFiles.DIRECTORY);
        if (!Files.exists(changesPaths)) {
            Files.createDirectory(changesPaths);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Knjigovodstvo - (c) Mario Kopjar 2022.");
        stage.setScene(scene);
        stage.show();
    }
}