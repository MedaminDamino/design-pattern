package logging;

import java.time.LocalDateTime;

/** ConsoleLoggingStrategy – Strategy Pattern concrete implementation. */
public class ConsoleLoggingStrategy implements LoggingStrategy {
    @Override
    public void log(String action, String details) {
        System.out.printf("[LOG][%s] %s – %s%n", LocalDateTime.now(), action, details);
    }
}
