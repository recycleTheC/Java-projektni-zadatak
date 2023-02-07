package hr.java.projekt.model.history;

import hr.java.projekt.util.dialog.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ChangeHistoryRecordFiles {
    public static final String DIRECTORY = "history";
    private static final Logger logger = LoggerFactory.getLogger(ChangeHistoryRecordFiles.class);

    public static <Record extends ChangeHistoryRecord<? extends WritableHistory>> void write(Record record) {
        Path path = Path.of(DIRECTORY, "change_" + record.getTimeStamp().format(DateTimeFormatter.ofPattern(ChangeHistoryRecord.DATE_TIME_FORMAT)) + ".dat");
        try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            writer.writeObject(record);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static ChangeHistoryRecord read(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(file))) {
            return (ChangeHistoryRecord) reader.readObject();
        }
    }

    public static List<ChangeHistoryRecord> readAll() throws IOException, ClassNotFoundException {
        List<ChangeHistoryRecord> records = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(DIRECTORY))) {
            for (Path path : stream) {
                try {
                    records.add(read(path.toFile()));
                } catch (InvalidClassException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return records;
    }
}
