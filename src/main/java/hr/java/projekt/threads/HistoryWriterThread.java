package hr.java.projekt.threads;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.model.history.ChangeHistoryRecord;
import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import hr.java.projekt.model.history.WritableHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public final class HistoryWriterThread<Type extends ChangeHistoryRecord<? extends WritableHistory>> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HistoryWriterThread.class);
    private final Thread thread;
    private final Type record;

    public HistoryWriterThread(Type record) {
        this.record = record;
        this.thread = new Thread(this);
    }

    public void start() {
        thread.start();
    }

    @Override
    public synchronized void run() {
        while (MainApplication.historyWritingInProgress) {
            try {
                wait(5000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        MainApplication.historyWritingInProgress = true;
        try {
            Path savedPath = ChangeHistoryRecordFiles.write(record);
            MainApplication.lastHistoryRecord = Optional.of(savedPath.toString());
            logger.debug("Promjena uspješno spremljena!");
        } catch (RuntimeException ex) {
            logger.error("Greška tijekom izvođenja dretve!", ex);
        }
        MainApplication.historyWritingInProgress = false;

        notifyAll();
    }
}
