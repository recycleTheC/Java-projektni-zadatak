package hr.java.projekt.util.dialog;

import hr.java.projekt.model.Entity;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.util.Optional;

public class SelectionDialog {
    public static <Model extends Entity, Controller extends CanReturnTableViewSelection<Model>> Optional<Model> showDialog(String title, String header, String fxml) throws IOException {
        Dialog<Model> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType confirmButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        FXMLLoader fxmlLoader = new FXMLLoader(SelectionDialog.class.getResource(fxml));
        dialog.getDialogPane().setContent(fxmlLoader.load());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                return fxmlLoader.<Controller>getController().getSelectedValue();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
