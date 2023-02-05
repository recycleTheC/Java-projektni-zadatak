/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * TextField implementation which accepts only formatted big decimal numbers
 * User input is formatted when the focus is lost, or when the user hits RETURN
 * Adapted according to article <a href="https://dzone.com/articles/javafx-numbertextfield-and">"JavaFX NumberTextField and Spinner Control"</a>
 *
 * @author Thomas Bolz
 * @author Mario Kopjar
 */
public class BigDecimalTextField extends TextField {
    private final NumberFormat numberFormat;
    private final ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();

    public final BigDecimal getNumber() {
        return number.get();
    }

    public final void setNumber(BigDecimal value) {
        number.set(value);
    }

    public ObjectProperty<BigDecimal> numberProperty() {
        return number;
    }

    public BigDecimalTextField() {
        this(new BigDecimal("0"));
    }

    public BigDecimalTextField(BigDecimal value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public BigDecimalTextField(BigDecimal value, NumberFormat nf) {
        super();
        this.numberFormat = nf;
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
            BigDecimal newValue = new BigDecimal(parsedNumber.toString()).setScale(2, RoundingMode.HALF_UP);

            setNumber(newValue);
            selectAll();
        } catch (ParseException | NumberFormatException ex) {
            // setting old value
            setText(numberFormat.format(number.get()));
        }
    }
}