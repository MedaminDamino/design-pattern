package patterns.template;

import model.AbstractShape;
import model.ShapeType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * PolygonShape – Template Method Pattern base for all polygon shapes.
 *
 * <p>This abstract class defines the <em>template method</em> {@link #draw(Pane)},
 * which outlines the fixed algorithm for rendering a polygon:</p>
 * <ol>
 *   <li>Compute the bounding box from the drag coordinates.</li>
 *   <li>Call the abstract {@link #generatePoints(double, double, double, double)}
 *       hook – implemented by each concrete subclass.</li>
 *   <li>Apply stroke/fill colors and add the {@link Polygon} to the pane.</li>
 * </ol>
 *
 * <p>Subclasses only override {@code generatePoints()}, never {@code draw()}.
 * This guarantees consistent visual behavior across all polygon types.</p>
 *
 * Open/Closed: new polygon shapes only need a subclass – no existing code changes.
 */
public abstract class PolygonShape extends AbstractShape {

    protected PolygonShape(ShapeType type, double sx, double sy, double ex, double ey,
                           String stroke, String fill) {
        super(type, sx, sy, ex, ey, stroke, fill);
    }

    /**
     * Template Method hook – returns flat coordinate array [x0,y0, x1,y1, …].
     *
     * @param x      left  of the bounding box
     * @param y      top   of the bounding box
     * @param width  width of the bounding box
     * @param height height of the bounding box
     */
    protected abstract double[] generatePoints(double x, double y, double width, double height);

    /**
     * Template Method – fixed drawing algorithm; subclasses must NOT override this.
     */
    @Override
    public void draw(Pane pane) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double w = Math.abs(endX - startX);
        double h = Math.abs(endY - startY);
        if (w < 4) w = 4;
        if (h < 4) h = 4;

        double[] pts = generatePoints(x, y, w, h);
        Polygon polygon = new Polygon(pts);
        polygon.setStroke(parseColor(strokeColor));
        polygon.setStrokeWidth(2);
        polygon.setFill("none".equalsIgnoreCase(fillColor) ? Color.TRANSPARENT : parseColor(fillColor));
        pane.getChildren().add(polygon);
        this.visualNode = polygon;
    }

    private Color parseColor(String c) {
        try { return Color.web(c); } catch (Exception e) { return Color.WHITE; }
    }
}
