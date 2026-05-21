package repository;

import model.Drawing;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface Segregation Principle: Small focused interface for Drawing persistence.
 */
public interface IDrawingRepository {
    Drawing save(Drawing drawing) throws SQLException;
    List<Drawing> findAll() throws SQLException;
    void deleteById(int id) throws SQLException;
}
