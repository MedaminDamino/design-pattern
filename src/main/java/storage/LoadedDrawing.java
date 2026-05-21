package storage;

import model.Drawing;
import model.DrawableShape;
import java.util.List;

/**
 * LoadedDrawing – A simple data container holding the loaded Drawing metadata
 * and its list of DrawableShapes. Helps unify Database and File strategies.
 */
public class LoadedDrawing {
    private final Drawing drawing;
    private final List<DrawableShape> shapes;

    public LoadedDrawing(Drawing drawing, List<DrawableShape> shapes) {
        this.drawing = drawing;
        this.shapes = shapes;
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public List<DrawableShape> getShapes() {
        return shapes;
    }
}
