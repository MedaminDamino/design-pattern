package patterns.strategy.logging;

import repository.ILogRepository;

/**
 * DatabaseLoggingStrategy – Strategy Pattern: persists logs to the database.
 * Dependency Inversion: depends on ILogRepository abstraction.
 */
public class DatabaseLoggingStrategy implements LoggingStrategy {
    private final ILogRepository logRepository;

    public DatabaseLoggingStrategy(ILogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void log(String action, String details) {
        logRepository.save(action, details);
    }
}
