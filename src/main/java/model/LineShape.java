package model;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/** LineShape – concrete DrawableShape for line segments. */
public class LineShape extends AbstractShape {

    public LineShape(double sx, double sy, double ex, double ey,
                     String stroke, String fill) {
        super(ShapeType.LINE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    public void draw(Pane pane) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(parseColor(strokeColor));
        line.setStrokeWidth(2);
        pane.getChildren().add(line);
        this.visualNode = line;
    }

    private Color parseColor(String c) {
        try { return Color.web(c); } catch (Exception e) { return Color.WHITE; }
    }
}
