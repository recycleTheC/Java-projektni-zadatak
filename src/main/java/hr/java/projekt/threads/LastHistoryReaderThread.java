/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.threads;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.model.history.ChangeHistoryRecord;
import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import hr.java.projekt.model.history.WritableHistory;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LastHistoryReaderThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(LastHistoryReaderThread.class);
    private final Thread thread;
    private Label label;
    private String lastReadRecord;

    public LastHistoryReaderThread(Label label) {
        this.label = label;
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    @Override
    public synchronized void run() {
        while (true) {
            while (MainApplication.historyWritingInProgress) {
                Platform.runLater(() -> label.setText("čekam..."));
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (MainApplication.lastHistoryRecord.isPresent()) {
                String path = MainApplication.lastHistoryRecord.get();

                Platform.runLater(() -> {
                    try {
                        ChangeHistoryRecord historyRecord = ChangeHistoryRecordFiles.read(new File(path));

                        LocalDateTime timestamp = historyRecord.getTimeStamp();
                        String description = "nepoznato";

                        if(historyRecord.getOldValue() != null){
                            if (historyRecord.getOldValue() instanceof WritableHistory record) {
                                description = record.getShortDescription();
                            }
                        } else if (historyRecord.getNewValue() != null) {
                            if (historyRecord.getNewValue() instanceof WritableHistory record) {
                                description = record.getShortDescription();
                            }
                        }

                        label.setText(description + " (" + timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")) + ")");
                    } catch (IOException | ClassNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    }
                });

                MainApplication.lastHistoryRecord = Optional.empty();
            }

            notifyAll();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
