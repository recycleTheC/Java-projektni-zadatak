/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.javafx;

import java.text.NumberFormat;
import java.text.ParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;

/**
 * TextField implementation which accepts only formatted long integers
 * User input is formatted when the focus is lost, or when the user hits RETURN
 * Adapted according to article <a href="https://dzone.com/articles/javafx-numbertextfield-and">"JavaFX NumberTextField and Spinner Control"</a>
 *
 * @author Thomas Bolz
 * @author Mario Kopjar
 */
public class LongNumberTextField extends TextField {
    private final NumberFormat numberFormat;
    private final ObjectProperty<Long> number = new SimpleObjectProperty<>();

    public final Long getNumber() {
        return number.get();
    }

    public final void setNumber(Long value) {
        number.set(value);
    }

    public ObjectProperty<Long> numberProperty() {
        return number;
    }

    public LongNumberTextField() {
        this(0L);
    }

    public LongNumberTextField(Long value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public LongNumberTextField(Long value, NumberFormat nf) {
        super();
        this.numberFormat = nf;
        this.numberFormat.setGroupingUsed(false);
        initHandlers();
        setNumber(value);
    }

    private void initHandlers() {
        setOnAction(arg -> parseAndFormatInput());

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) parseAndFormatInput();
        });

        numberProperty().addListener((observable, oldValue, newValue) -> setText(numberFormat.format(newValue)));
    }

    private void parseAndFormatInput() {
        try {
            String input = getText();
            if (input == null || input.length() == 0) return;

            Number parsedNumber = numberFormat.parse(input);
            Long newValue = Long.valueOf(parsedNumber.toString());

            setNumber(newValue);
            selectAll();
        } catch (ParseException | NumberFormatException ex) {
            // setting old value
            setText(numberFormat.format(number.get()));
        }
    }
}