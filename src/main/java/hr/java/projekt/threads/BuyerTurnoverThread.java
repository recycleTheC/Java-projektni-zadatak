/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.threads;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.database.BusinessRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.business.Business;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BuyerTurnoverThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(BuyerTurnoverThread.class);
    private final Thread thread;
    private BarChart<String, Double> buyerChart;

    public BuyerTurnoverThread(BarChart<String, Double> buyerChart) {
        this.buyerChart = buyerChart;
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
        BusinessRepository partnersRepository = new BusinessRepository();
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

                MainApplication.refreshHomeScreen = false;
                Thread.sleep(1500);
            } catch (InterruptedException | DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
