package hr.java.projekt.model.history;

import hr.java.projekt.model.Entity;

public final class DeletedChangeRecord<T extends Entity> extends ChangeHistoryRecord<T> {
    public DeletedChangeRecord(T oldValue) {
        super(oldValue, null);
    }
}
