package hr.java.projekt.util.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class MessageBox {
    private static Alert create(String title, String header, String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert;
    }
    public static void show(String title, String header, String content, Alert.AlertType type) {
        Alert alert = create(title, header, content, type);
        alert.showAndWait();
    }

    public static void show(String title, String header, String content){
        show(title, header, content, Alert.AlertType.INFORMATION);
    }

    public static void show(String title, String header, String content, Exception exception) {
        Alert alert = create(title, header, content, Alert.AlertType.ERROR);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane exceptionContent = new GridPane();
        exceptionContent.setMaxWidth(Double.MAX_VALUE);
        exceptionContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(exceptionContent);

        alert.showAndWait();
    }
}
