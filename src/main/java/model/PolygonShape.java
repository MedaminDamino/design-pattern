package model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * PolygonShape – Template Method base for polygon shapes.
 * Subclasses implement {@link #generatePoints(double, double, double, double)}
 * to return the polygon vertex coordinates. Everything else (color, pane
 * attachment, visual-node bookkeeping) is handled here.
 *
 * Open/Closed: new polygon shapes only need a subclass – no existing code changes.
 */
public abstract class PolygonShape extends AbstractShape {

    protected PolygonShape(ShapeType type, double sx, double sy, double ex, double ey,
                           String stroke, String fill) {
        super(type, sx, sy, ex, ey, stroke, fill);
    }

    /**
     * Returns flat coordinate array [x0,y0, x1,y1, …] for the polygon.
     *
     * @param x      left  of the bounding box
     * @param y      top   of the bounding box
     * @param width  width of the bounding box
     * @param height height of the bounding box
     */
    protected abstract double[] generatePoints(double x, double y, double width, double height);

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
