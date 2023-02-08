package hr.java.projekt.controller;

import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.threads.HomeScreenThread;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HomeController {
    /*@FXML
    private BarChart<Integer, Integer> operatorChart;*/
    @FXML
    private BarChart<String, Double> buyerChart;
    @FXML
    private TableView<Invoice> dueInvoicesTable;
    @FXML
    private TableColumn<Invoice, String> partnerDueInvoicesColumn;
    @FXML
    private TableColumn<Invoice, String> documentDueInvoicesColumn;
    @FXML
    private TableColumn<Invoice, BigDecimal> amountDueInvoicesColumn;
    @FXML
    private TableColumn<Invoice, LocalDate> dateDueInvoicesColumn;

    @FXML
    private void initialize(){
        buyerChart.getYAxis().setLabel("Promet");
        buyerChart.setLegendVisible(false);
        buyerChart.setAnimated(false);

        documentDueInvoicesColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShortDescription()));
        amountDueInvoicesColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStaticAmount()));
        dateDueInvoicesColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDueDate()));
        partnerDueInvoicesColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPartner().getName()));

        new HomeScreenThread(buyerChart, dueInvoicesTable).start();
    }
}
