package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.ArticleSelectionDialog;
import javafx.scene.control.TableView;

public sealed interface CanReturnTableViewSelection<T> permits ArticleSelectionDialog {
    TableView<T> getSelectedValue();
}
