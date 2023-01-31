package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.AccountSelectionDialogController;
import hr.java.projekt.controller.ArticleSelectionDialogController;
import hr.java.projekt.controller.PartnerSelectionDialogController;

public sealed interface CanReturnTableViewSelection<T> permits AccountSelectionDialogController, ArticleSelectionDialogController, PartnerSelectionDialogController {
    T getSelectedValue();
}
