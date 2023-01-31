package hr.java.projekt.model.history;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ChangeHistoryRecordFiles {
    public static final String DIRECTORY = "history";
    public static <Record extends ChangeHistoryRecord<? extends WritableHistory>> void write(Record record){
        Path path = Path.of(DIRECTORY, "change_" + record.getTimeStamp().format(DateTimeFormatter.ofPattern(ChangeHistoryRecord.DATE_TIME_FORMAT)) + ".dat");
        try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(path.toFile()))){
            writer.writeObject(record);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangeHistoryRecord read(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(file))){
            return (ChangeHistoryRecord) reader.readObject();
        }
    }

    public static List<ChangeHistoryRecord> readAll() throws IOException, ClassNotFoundException {
        List<ChangeHistoryRecord> records = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(DIRECTORY))){
            for (Path path : stream) {
                records.add(read(path.toFile()));
            }
        }
        return records;
    }
}
