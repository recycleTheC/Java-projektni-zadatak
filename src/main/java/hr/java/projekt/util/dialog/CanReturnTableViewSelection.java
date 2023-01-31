package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.ArticleSelectionDialogController;
import hr.java.projekt.controller.PartnerSelectionDialogController;

public sealed interface CanReturnTableViewSelection<T> permits ArticleSelectionDialogController, PartnerSelectionDialogController {
    T getSelectedValue();
}
