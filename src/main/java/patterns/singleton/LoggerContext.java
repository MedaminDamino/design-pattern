package patterns.singleton;

import patterns.strategy.logging.LoggingStrategy;
import patterns.strategy.logging.ConsoleLoggingStrategy;

/**
 * LoggerContext – Singleton Pattern + Strategy Pattern context.
 * Holds the active LoggingStrategy and delegates all log calls to it.
 *
 * Singleton: only one instance exists for the entire application lifetime.
 * Strategy:  the active logging strategy can be swapped at runtime.
 *
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
