package hr.java.projekt.controller;

import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.articles.Asset;
import hr.java.projekt.model.articles.Service;
import hr.java.projekt.util.CustomStringConverters;
import hr.java.projekt.util.dialog.SelectionDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ArticleEditorController {
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField measureField;
    @FXML
    private Spinner<Double> priceField;
    @FXML
    private Spinner<Double> priceVATField;
    @FXML
    private ChoiceBox<TaxRate> taxRateBox;
    @FXML
    private RadioButton assetRadio;
    @FXML
    private RadioButton serviceRadio;
    @FXML
    private Spinner<Double> costField;
    @FXML
    private Text serviceText;

    public void initialize() {
        taxRateBox.setItems(FXCollections.observableArrayList(TaxRate.values()));
        taxRateBox.setConverter(CustomStringConverters.TAX_RATE_STRING_CONVERTER);

        priceField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,0));
        priceVATField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,0));
        costField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,0));
    }

    public void selectArticle() throws IOException {
        Optional<Article> selection = SelectionDialog.showDialog("Artikl", "Odabir artikla", "article-selection-dialog.fxml");

        if(selection.isPresent()){
            Article article = selection.get();

            codeField.setText(article.getCode());
            nameField.setText(article.getName());
            measureField.setText(article.getMeasure());
            priceField.getValueFactory().setValue(article.getPrice().doubleValue());
            priceVATField.getValueFactory().setValue(article.getPriceWithVAT().doubleValue());
            taxRateBox.setValue(article.getTaxRate());

            if(article instanceof Asset) {
                assetRadio.setSelected(true);
                costField.setVisible(false);
                serviceText.setVisible(false);
            }
            else if(article instanceof Service service) {
                serviceRadio.setSelected(true);
                costField.setVisible(true);
                serviceText.setVisible(true);
                costField.getValueFactory().setValue(service.getCostOfService().doubleValue());
            }
        }
    }
}
