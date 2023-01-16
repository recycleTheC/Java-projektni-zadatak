package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.ArticleSelectionDialog;
import hr.java.projekt.model.Entity;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class SelectionDialog {
    public static <Model extends Entity, Controller extends CanReturnTableViewSelection<Model>> Optional<Model> showDialog(String title, String header, String fxml) throws IOException {
        Dialog<Model> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType confirmButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        FXMLLoader fxmlLoader = new FXMLLoader(ArticleSelectionDialog.class.getResource(fxml));
        dialog.getDialogPane().setContent(fxmlLoader.load());

        TableView<Model> tableView = fxmlLoader.<Controller>getController().getSelectedValue();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                return tableView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
