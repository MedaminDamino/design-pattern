package model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

/** Ellipse (distinct from Circle – uses full bounding box independently). */
public class EllipseShape extends AbstractShape {

    public EllipseShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.ELLIPSE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    public void draw(Pane pane) {
        double cx = (startX + endX) / 2.0;
        double cy = (startY + endY) / 2.0;
        double rx = Math.max(Math.abs(endX - startX) / 2.0, 4);
        double ry = Math.max(Math.abs(endY - startY) / 2.0, 4);
        Ellipse ellipse = new Ellipse(cx, cy, rx, ry);
        ellipse.setStroke(parseColor(strokeColor));
        ellipse.setStrokeWidth(2);
        ellipse.setFill("none".equalsIgnoreCase(fillColor) ? Color.TRANSPARENT : parseColor(fillColor));
        pane.getChildren().add(ellipse);
        this.visualNode = ellipse;
    }

    private Color parseColor(String c) {
        try { return Color.web(c); } catch (Exception e) { return Color.WHITE; }
    }
}
