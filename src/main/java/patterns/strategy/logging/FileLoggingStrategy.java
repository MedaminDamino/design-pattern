package patterns.strategy.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** FileLoggingStrategy – Strategy Pattern: writes log to logs/actions.log file. */
public class FileLoggingStrategy implements LoggingStrategy {
    private static final String LOG_FILE = "logs/actions.log";

    @Override
    public void log(String action, String details) {
        try {
            File file = new File(LOG_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                pw.printf("[%s] [%s] %s%n", timestamp, action, details);
            }
        } catch (IOException e) {
            System.err.println("FileLoggingStrategy error: " + e.getMessage());
        }
    }
}
