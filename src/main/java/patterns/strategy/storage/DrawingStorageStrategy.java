package patterns.strategy.storage;

import model.Drawing;
import javafx.scene.layout.Pane;
import java.util.List;

/**
 * DrawingStorageStrategy – Strategy Pattern interface for persistence.
 * Separates save/open mechanisms (Database vs File) from business logic.
 */
public interface DrawingStorageStrategy {
    boolean save(Drawing drawing, Pane shapePane, boolean saveAs) throws Exception;
    List<Drawing> listSaved() throws Exception;
    LoadedDrawing load(Drawing selectedDrawing, Pane shapePane) throws Exception;
    String getModeName();
}
