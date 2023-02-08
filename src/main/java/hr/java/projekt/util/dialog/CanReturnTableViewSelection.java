package hr.java.projekt.util.dialog;

import hr.java.projekt.controller.*;

public sealed interface CanReturnTableViewSelection<T> permits AccountSelectionDialogController, ArticleSelectionDialogController, AssetInputSelectionDialogController, InvoiceOutputSelectionDialogController, PartnerSelectionDialogController {
    T getSelectedValue();
}
