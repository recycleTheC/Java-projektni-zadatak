package hr.java.projekt.util.dialog;

import javafx.scene.control.*;

import java.util.Optional;

public class ConfirmationDialog {
    public static boolean showDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }
}
