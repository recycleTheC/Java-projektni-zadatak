/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.threads;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.database.BusinessRepository;
import hr.java.projekt.database.InvoiceOutputRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.model.invoices.InvoiceOutput;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HomeScreenThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(HomeScreenThread.class);
    private final Thread thread;
    private final BarChart<String, Double> buyerChart;
    private final TableView<Invoice> dueInvoicesTable;

    public HomeScreenThread(BarChart<String, Double> buyerChart, TableView<Invoice> dueInvoicesTable) {
        this.buyerChart = buyerChart;
        this.dueInvoicesTable = dueInvoicesTable;
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
        BusinessRepository partnersRepository = new BusinessRepository();
        InvoiceOutputRepository invoiceOutputRepository = new InvoiceOutputRepository();

        while (true) {
            while (!MainApplication.refreshHomeScreen) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            try {
                List<Business> partners = partnersRepository.getMany();
                XYChart.Series<String, Double> series = new XYChart.Series<>();

                for (Business partner : partners) {
                    Double turnover = partnersRepository.getTurnoverForPartner(partner.getId());
                    String name = partner.getName();

                    if(name.length() > 30){
                        name = name.replace(" ", "\n");
                    }

                    if (turnover > 0) {
                        series.getData().add(new XYChart.Data<>(name, turnover));
                    }
                }

                Platform.runLater(() -> {
                    buyerChart.getData().clear();
                    buyerChart.getData().add(series);
                });
            } catch (DatabaseException e) {
                logger.error(e.getMessage(), e);
            }

            try {
                List<InvoiceOutput> dueInvoices = invoiceOutputRepository.getDueInvoices();

                Platform.runLater(() -> {
                    dueInvoicesTable.setItems(FXCollections.observableArrayList(dueInvoices));
                });
            } catch (DatabaseException e) {
                logger.error(e.getMessage(), e);
            }

            MainApplication.refreshHomeScreen = false;
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
