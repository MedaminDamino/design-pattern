package model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** RectangleShape – concrete DrawableShape. Open/Closed: new shapes don't touch this. */
public class RectangleShape extends AbstractShape {

    public RectangleShape(double sx, double sy, double ex, double ey,
                          String stroke, String fill) {
        super(ShapeType.RECTANGLE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    public void draw(Pane pane) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setStroke(parseColor(strokeColor));
        rect.setFill("none".equalsIgnoreCase(fillColor) ? Color.TRANSPARENT : parseColor(fillColor));
        rect.setStrokeWidth(2);
        pane.getChildren().add(rect);
        this.visualNode = rect;
    }

    private Color parseColor(String c) {
        try { return Color.web(c); } catch (Exception e) { return Color.WHITE; }
    }
}
