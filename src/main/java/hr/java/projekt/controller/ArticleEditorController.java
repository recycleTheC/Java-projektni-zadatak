package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.*;
import hr.java.projekt.model.history.*;
import hr.java.projekt.threads.HistoryWriterThread;
import hr.java.projekt.util.CustomStringConverters;
import hr.java.projekt.util.dialog.ConfirmationDialog;
import hr.java.projekt.util.dialog.MessageBox;
import hr.java.projekt.util.dialog.SelectionDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ArticleEditorController implements CanSetTabTitle {
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
    private Text serviceLabel;
    @FXML
    private Button deleteButton;

    private ArticleRepository articleRepository;
    private Optional<Article> savedItem;
    private Tab tab;
    private static final Logger logger = LoggerFactory.getLogger(ArticleEditorController.class);

    @FXML
    private void initialize() {
        savedItem = Optional.empty();
        articleRepository = new ArticleRepository();
        taxRateBox.setItems(FXCollections.observableArrayList(TaxRate.values()));
        taxRateBox.setConverter(CustomStringConverters.TaxRateStringConverter);

        priceField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        priceVATField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        costField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));

        priceField.valueProperty().addListener((object, oldValue, newValue) -> {
            BigDecimal price = BigDecimal.valueOf(object.getValue());
            BigDecimal rate = BigDecimal.ONE;

            if (Optional.ofNullable(taxRateBox.getValue()).isPresent())
                rate = taxRateBox.getValue().getRate().add(BigDecimal.ONE);
            priceVATField.getValueFactory().setValue(price.multiply(rate).doubleValue());
        });

        priceVATField.valueProperty().addListener((object, oldValue, newValue) -> {
            BigDecimal priceVAT = BigDecimal.valueOf(object.getValue());
            BigDecimal rate = BigDecimal.ONE;

            if (Optional.ofNullable(taxRateBox.getValue()).isPresent())
                rate = taxRateBox.getValue().getRate().add(BigDecimal.ONE);
            priceField.getValueFactory().setValue(priceVAT.divide(rate, RoundingMode.HALF_UP).doubleValue());
        });

        taxRateBox.valueProperty().addListener((object, oldValue, newValue) -> {
            if (Optional.ofNullable(taxRateBox.getValue()).isPresent()) {
                BigDecimal price = BigDecimal.valueOf(priceField.getValue());
                BigDecimal rate = taxRateBox.getValue().getRate().add(BigDecimal.ONE);
                priceVATField.getValueFactory().setValue(price.multiply(rate).doubleValue());
            }
        });

        codeField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if (unfocused) {
                try {
                    Optional<Article> article = articleRepository.get(codeField.getText());
                    article.ifPresent(this::setFields);
                } catch (DatabaseException e) {
                    logger.error(e.getMessage(), e.getCause());
                    MessageBox.show("Artikl", "Pogre??ka u radu s bazom podataka", "Nije mogu??e napraviti provjeru u bazi podataka!\n" + e.getCause().getMessage());
                }
            }
        });

        serviceRadio.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            costField.setVisible(true);
            serviceLabel.setVisible(true);
            costField.getValueFactory().setValue((double) 0);
        }));

        assetRadio.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            costField.setVisible(false);
            serviceLabel.setVisible(false);
            costField.getValueFactory().setValue((double) 0);
        }));

        deleteButton.setDisable(true);
    }

    @FXML
    private void selectArticle() throws IOException {
        Optional<Article> selection = SelectionDialog.showDialog("Artikl", "Odabir artikla", "article-selection-dialog.fxml");
        selection.ifPresent(this::setFields);
    }

    @FXML
    private void saveArticle() {
        String code = codeField.getText();
        String name = nameField.getText();
        String measure = measureField.getText();
        BigDecimal price = BigDecimal.valueOf(priceField.getValue());
        TaxRate rate = taxRateBox.getValue();

        StringBuilder validationError = new StringBuilder();

        if (code.isBlank()) validationError.append("??ifra artikla nije navedena!\n");
        if (name.isBlank()) validationError.append("Naziv artikla nije naveden!\n");
        if (measure.isBlank()) validationError.append("Mjerna jedinica za artikl nije navedena!\n");
        if (price.equals(BigDecimal.ZERO)) validationError.append("Cijena artikla ne smije biti 0!\n");
        if (Optional.ofNullable(rate).isEmpty()) validationError.append("Porezna stopa za artikl nije navedena!\n");

        if (!validationError.isEmpty()) {
            MessageBox.show("Artikl", "Neispravne vrijednosti", validationError.toString(), Alert.AlertType.WARNING);
            return;
        }

        boolean confirmation = ConfirmationDialog.showDialog("Artikl", "Spremanje artikla", "??elite li spremiti artikl '" + code + "'?");
        if (!confirmation) return;

        Article article;

        if (assetRadio.isSelected()) {
            article = new AssetBuilder().setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(rate).build();
        } else {
            ServiceBuilder builder = new ServiceBuilder().setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(rate);
            Double cost = costField.getValue();

            if (Optional.ofNullable(cost).isPresent()) {
                builder.setCostOfService(BigDecimal.valueOf(cost));
            }

            article = builder.build();
        }

        try {
            if (savedItem.isPresent()) {
                articleRepository.update(savedItem.get().getId(), article);
                new HistoryWriterThread<>(new UpdatedChangeRecord<>(savedItem.get(), article)).start();
            } else {
                articleRepository.save(article);
                new HistoryWriterThread<>(new CreatedChangeRecord<>(article)).start();
            }

            MessageBox.show("Artikl", "Spremanje artikla", "Artikl " + article.getCode() + " uspje??no spremljen!");
            resetFields();
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Artikl", "Pogre??ka u radu s bazom podataka", "Artikl " + article.getCode() + "nije spremljen!", ex);
        }
    }

    @FXML
    private void deleteArticle() {
        boolean result = ConfirmationDialog.showDialog("Artikl", "Brisanje artikla", "Jeste li sigurni da ??elite obrisati artikl '" + codeField.getText() + "'?");

        if (result) {
            try {
                articleRepository.delete(savedItem.get());
                new HistoryWriterThread<>(new DeletedChangeRecord<>(savedItem.get())).start();
                MessageBox.show("Artikl", "Brisanje artikla", "Artikl " + savedItem.get().getCode() + " uspje??no obrisan!");
                resetFields();
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Artikl", "Pogre??ka u radu s bazom podataka", "Artikl " + savedItem.get().getCode() + "se ne mo??e obrisati iz baze podataka!", ex);
            }
        }
    }

    @FXML
    private void cancelEdit() {
        if (savedItem.isEmpty() || codeField.getText().isEmpty()) return;
        boolean confirmation = ConfirmationDialog.showDialog("Artikl", "Odustajanje", "Jeste li sigurni da ??elite odustati od ure??ivanja artikla?");
        if (confirmation) resetFields();
    }

    @FXML
    private void resetFields() {
        setTabTitle("Artikli");
        savedItem = Optional.empty();
        codeField.setText("");
        codeField.setDisable(false);
        deleteButton.setDisable(true);

        nameField.setText("");
        measureField.setText("");
        priceField.getValueFactory().setValue((double) 0);
        taxRateBox.getSelectionModel().clearSelection();

        assetRadio.setSelected(false);
        serviceRadio.setSelected(false);
        costField.setVisible(false);
        serviceLabel.setVisible(false);
        costField.getValueFactory().setValue((double) 0);
    }

    private void setFields(Article article) {
        setTabTitle("Artikl '" + article.getCode() + "'");
        savedItem = Optional.of(article);
        codeField.setText(article.getCode());
        codeField.setDisable(true);
        deleteButton.setDisable(false);

        nameField.setText(article.getName());
        measureField.setText(article.getMeasure());
        priceField.getValueFactory().setValue(article.getPrice().doubleValue());
        taxRateBox.getSelectionModel().select(article.getTaxRate());

        if (article instanceof Asset) {
            assetRadio.setSelected(true);
            costField.setVisible(false);
            serviceLabel.setVisible(false);
        } else if (article instanceof Service service) {
            serviceRadio.setSelected(true);
            costField.setVisible(true);
            serviceLabel.setVisible(true);
            costField.getValueFactory().setValue(service.getCostOfService().doubleValue());
        }
    }

    @Override
    public void storeTabReference(Tab tab) {
        this.tab = tab;
    }

    @Override
    public void setTabTitle(String title) {
        this.tab.setText(title);
    }
}
