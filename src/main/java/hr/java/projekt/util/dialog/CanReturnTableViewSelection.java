package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.ArticleSelectionDialogController;
import javafx.scene.control.TableView;

public sealed interface CanReturnTableViewSelection<T> permits ArticleSelectionDialogController {
    TableView<T> getSelectedValue();
}
