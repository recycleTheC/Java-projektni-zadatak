package hr.java.projekt.model.history;

import hr.java.projekt.model.Entity;

public final class UpdatedChangeRecord<T extends Entity> extends ChangeHistoryRecord<T> {
    public UpdatedChangeRecord(T oldValue, T newValue) {
        super(oldValue, newValue);
    }
}
