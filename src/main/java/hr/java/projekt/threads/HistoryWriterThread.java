package hr.java.projekt.threads;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.history.ChangeHistoryRecord;
import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HistoryWriterThread<Type extends ChangeHistoryRecord<? extends Entity>> implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(HistoryWriterThread.class);
    private final Thread thread;
    private final Type record;

    public HistoryWriterThread(Type record) {
        this.record = record;
        this.thread = new Thread(this);
    }

    public void start(){
        thread.start();
    }

    @Override
    public void run() {
        try {
            ChangeHistoryRecordFiles.write(record);
            logger.debug("Promjena uspješno spremljena!");
        }
        catch (RuntimeException ex){
            logger.error("Greška tijekom izvođenja dretve!", ex);
        }
    }
}
