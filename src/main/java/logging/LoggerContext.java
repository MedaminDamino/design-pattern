package logging;

/**
 * LoggerContext – Strategy Pattern context (Singleton).
 * Holds the active LoggingStrategy and delegates all log calls to it.
 * Dependency Inversion: controllers call this, never concrete loggers.
 */
public class LoggerContext {
    private static final LoggerContext INSTANCE = new LoggerContext();
    private LoggingStrategy strategy = new ConsoleLoggingStrategy(); // default

    private LoggerContext() {}

    public static LoggerContext getInstance() { return INSTANCE; }

    /** Swap logging strategy at runtime – Strategy Pattern hot-swap. */
    public void setStrategy(LoggingStrategy strategy) {
        this.strategy = strategy;
    }

    public void log(String action, String details) {
        if (strategy != null) strategy.log(action, details);
    }
}
