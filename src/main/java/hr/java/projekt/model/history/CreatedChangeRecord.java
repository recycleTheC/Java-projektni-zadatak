package hr.java.projekt.model.history;

import hr.java.projekt.model.Entity;

public final class CreatedChangeRecord<T extends Entity> extends ChangeHistoryRecord<T> {
    public CreatedChangeRecord(T newValue) {
        super(null, newValue);
    }
}
