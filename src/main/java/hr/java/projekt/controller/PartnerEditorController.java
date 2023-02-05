package hr.java.projekt.controller;

import hr.java.projekt.database.BusinessRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.javafx.LongNumberTextField;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.business.BusinessBuilder;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.DeletedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.threads.HistoryWriterThread;
import hr.java.projekt.util.dialog.ConfirmationDialog;
import hr.java.projekt.util.dialog.MessageBox;
import hr.java.projekt.util.dialog.SelectionDialog;
import hr.java.projekt.exceptions.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.exceptions.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public final class PartnerEditorController implements CanSetTabTitle {
    @FXML
    private LongNumberTextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalField;
    @FXML
    private TextField oibField;
    @FXML
    private TextField ibanField;
    @FXML
    private Button deleteButton;

    private BusinessRepository partnerRepository;
    private Optional<Business> savedItem;
    private Tab tab;
    private static final Logger logger = LoggerFactory.getLogger(PartnerEditorController.class);

    @FXML
    private void initialize() {
        savedItem = Optional.empty();
        partnerRepository = new BusinessRepository();

        idField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if (unfocused) {
                try {
                    Long id = idField.getNumber();
                    if (id.equals(0L)) return;

                    Optional<Business> business = partnerRepository.get(id);

                    if (business.isPresent()) {
                        setFields(business.get());
                    } else {
                        boolean confirmation = ConfirmationDialog.showDialog("Partner", "Nepoznati partner", "Partner ne postoji u bazi podataka.\nŽelite li nastaviti unos za novog partnera?");
                        if (confirmation) idField.setNumber(0L);
                    }
                } catch (DatabaseException e) {
                    logger.error(e.getMessage(), e.getCause());
                    MessageBox.show("Partner", "Pogreška u radu s bazom podataka", "Nije moguće napraviti provjeru u bazi podataka!\n" + e.getCause().getMessage());
                }
            }
        });

        oibField.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (oldValue) {
                String oib = oibField.getText();
                try {
                    if (!oib.isBlank()) OIBValidator.validate(oib);
                } catch (OIBValidationException ex) {
                    MessageBox.show("Partner", "Neispravan OIB", ex.getMessage());
                }
            }
        }));

        ibanField.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (oldValue) {
                String iban = ibanField.getText();
                try {
                    if (!iban.isBlank()) IBANValidator.validate(iban);
                } catch (IBANValidationException ex) {
                    MessageBox.show("Partner", "Neispravan IBAN", ex.getMessage());
                }
            }
        }));

        deleteButton.setDisable(true);
    }

    @FXML
    private void selectAction() throws IOException {
        Optional<Business> selection = SelectionDialog.showDialog("Partner", "Odabir partnera", "partner-selection-dialog.fxml");
        selection.ifPresent(this::setFields);
    }

    @FXML
    private void saveAction() {
        String name = nameField.getText();
        String address = addressField.getText();
        String postal = postalField.getText();
        String oib = oibField.getText();
        String iban = ibanField.getText();

        StringBuilder validationError = new StringBuilder();

        if (name.isBlank()) validationError.append("Naziv partnera nije naveden!\n");
        if (address.isBlank()) validationError.append("Adresa partnera nije navedena!\n");
        if (postal.isBlank()) validationError.append("Poštanski broj i mjesto nije navedeno!\n");
        if (oib.isBlank()) validationError.append("OIB nije naveden!\n");
        else {
            try {
                OIBValidator.validate(oib);
            } catch (OIBValidationException e) {
                validationError.append(e.getMessage()).append("\n");
            }
        }
        if (iban.isBlank()) validationError.append("IBAN nije naveden!\n");
        else {
            try {
                IBANValidator.validate(iban);
            } catch (IBANValidationException e) {
                validationError.append(e.getMessage()).append("\n");
            }
        }

        if (!validationError.isEmpty()) {
            MessageBox.show("Partner", "Neispravne vrijednosti", validationError.toString(), Alert.AlertType.WARNING);
            return;
        }

        boolean confirmation = ConfirmationDialog.showDialog("Partner", "Spremanje partnera", "Želite li spremiti partnera '" + name + "'?");
        if (!confirmation) return;

        Business partner = new BusinessBuilder().setName(name).setAddress(address).setPostalCodeAndTown(postal).setUncheckedOIB(oib).setUncheckedIBAN(iban).build();

        try {
            if (savedItem.isPresent()) {
                partnerRepository.update(savedItem.get().getId(), partner);
                new HistoryWriterThread<>(new UpdatedChangeRecord<>(savedItem.get(), partner)).start();
            } else {
                partnerRepository.save(partner);
                new HistoryWriterThread<>(new CreatedChangeRecord<>(partner)).start();
            }

            MessageBox.show("Partner", "Spremanje partnera", "Partner " + partner.getName() + " uspješno spremljen!");
            resetFields();
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Partner", "Pogreška u radu s bazom podataka", "Partner " + partner.getName() + " nije spremljen!", ex);
        }
    }

    @FXML
    private void deleteAction() {
        boolean result = ConfirmationDialog.showDialog("Partner", "Brisanje partnera", "Jeste li sigurni da želite obrisati partnera '" + nameField.getText() + "'?");

        if (result) {
            try {
                partnerRepository.delete(savedItem.get());
                new HistoryWriterThread<>(new DeletedChangeRecord<>(savedItem.get())).start();
                MessageBox.show("Partner", "Brisanje partnera", "Partner " + savedItem.get().getName() + " uspješno obrisan!");
                resetFields();
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Partner", "Pogreška u radu s bazom podataka", "Partner " + savedItem.get().getName() + "se ne može obrisati iz baze podataka!", ex);
            }
        }
    }

    @FXML
    private void cancelEdit() {
        if (savedItem.isEmpty() || idField.getNumber() != 0) return;
        boolean confirmation = ConfirmationDialog.showDialog("Partner", "Odustajanje", "Jeste li sigurni da želite odustati od uređivanja partnera?");
        if (confirmation) resetFields();
    }

    @FXML
    private void resetFields() {
        setTabTitle("Partneri");
        savedItem = Optional.empty();

        idField.setNumber(0L);
        idField.setDisable(false);
        deleteButton.setDisable(true);

        nameField.setText("");
        addressField.setText("");
        postalField.setText("");
        oibField.setText(null);
        ibanField.setText(null);
    }

    private void setFields(Business partner) {
        setTabTitle("Partner '" + partner.getId() + "'");
        savedItem = Optional.of(partner);
        idField.setNumber(partner.getId());
        idField.setDisable(true);
        deleteButton.setDisable(false);

        nameField.setText(partner.getName());
        addressField.setText(partner.getAddress());
        postalField.setText(partner.getPostalCodeAndTown());
        oibField.setText(partner.getOIB());
        ibanField.setText(partner.getIBAN());
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
