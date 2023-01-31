package hr.java.projekt.model.history;

import hr.java.projekt.model.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class ChangeHistoryRecord<Value extends Entity> implements Serializable {
    public static String DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private final LocalDateTime timeStamp;
    private final Value oldValue, newValue;

    public ChangeHistoryRecord(LocalDateTime timeStamp, Value oldValue, Value newValue) {
        this.timeStamp = timeStamp;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ChangeHistoryRecord(Value oldValue, Value newValue) {
        this.timeStamp = LocalDateTime.now();
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public Value getOldValue() {
        return oldValue;
    }

    public Value getNewValue() {
        return newValue;
    }
}
