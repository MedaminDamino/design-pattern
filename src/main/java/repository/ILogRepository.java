package repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface Segregation Principle: Small focused interface for Log persistence.
 */
public interface ILogRepository {
    void save(String action, String details);
    List<String> findAll() throws SQLException;
}
