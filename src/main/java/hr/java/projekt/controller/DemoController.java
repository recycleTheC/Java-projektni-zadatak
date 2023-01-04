package hr.java.projekt.controller;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class DemoController {
    private Integer varijabla = 0;
    @FXML
    private Text textField;

    public void initialize(){
        varijabla++;
        textField.setText(varijabla.toString());
    }
}
