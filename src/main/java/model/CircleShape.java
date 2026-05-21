package model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/** CircleShape – concrete DrawableShape for circular geometry. */
public class CircleShape extends AbstractShape {

    public CircleShape(double sx, double sy, double ex, double ey,
                       String stroke, String fill) {
        super(ShapeType.CIRCLE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    public void draw(Pane pane) {
        double cx = (startX + endX) / 2.0;
        double cy = (startY + endY) / 2.0;
        double r  = Math.max(Math.min(width, height) / 2.0, 5);
        this.radius = r;
        Circle circle = new Circle(cx, cy, r);
        circle.setStroke(parseColor(strokeColor));
        circle.setFill("none".equalsIgnoreCase(fillColor) ? Color.TRANSPARENT : parseColor(fillColor));
        circle.setStrokeWidth(2);
        pane.getChildren().add(circle);
        this.visualNode = circle;
    }

    private Color parseColor(String c) {
        try { return Color.web(c); } catch (Exception e) { return Color.WHITE; }
    }
}
