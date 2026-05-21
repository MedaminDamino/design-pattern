package storage;

import model.Drawing;
import javafx.scene.layout.Pane;
import java.util.List;

/**
 * StorageContext – Strategy Pattern Context for Drawing persistence.
 * Delegates actual operations to the currently active DrawingStorageStrategy.
 */
public class StorageContext {
    private DrawingStorageStrategy strategy;

    public StorageContext(DrawingStorageStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DrawingStorageStrategy strategy) {
        this.strategy = strategy;
    }

    public DrawingStorageStrategy getStrategy() {
        return strategy;
    }

    public boolean save(Drawing drawing, Pane shapePane, boolean saveAs) throws Exception {
        return strategy.save(drawing, shapePane, saveAs);
    }

    public List<Drawing> listSaved() throws Exception {
        return strategy.listSaved();
    }

    public LoadedDrawing load(Drawing selectedDrawing, Pane shapePane) throws Exception {
        return strategy.load(selectedDrawing, shapePane);
    }
}
