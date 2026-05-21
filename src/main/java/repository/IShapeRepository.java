package repository;

import model.DrawableShape;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface Segregation Principle: Small focused interface for Shape persistence.
 */
public interface IShapeRepository {
    void saveAll(int drawingId, List<DrawableShape> shapes) throws SQLException;
    List<DrawableShape> findByDrawingId(int drawingId) throws SQLException;
}
