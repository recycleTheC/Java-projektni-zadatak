package hr.java.projekt.controller;

import hr.java.projekt.threads.BuyerTurnoverThread;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;

public class HomeController {
    /*@FXML
    private BarChart<Integer, Integer> operatorChart;*/
    @FXML
    private BarChart<String, Double> buyerChart;

    @FXML
    private void initialize(){
        buyerChart.getXAxis().setLabel("Kupac");
        buyerChart.getYAxis().setLabel("Promet");
        buyerChart.setLegendVisible(false);
        buyerChart.setAnimated(false);

        new BuyerTurnoverThread(buyerChart).start();
    }
}
