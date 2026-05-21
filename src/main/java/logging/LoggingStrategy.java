package logging;

/**
 * LoggingStrategy – Strategy Pattern interface (ISP).
 * Dependency Inversion: controllers depend on this abstraction.
 */
public interface LoggingStrategy {
    void log(String action, String details);
}
