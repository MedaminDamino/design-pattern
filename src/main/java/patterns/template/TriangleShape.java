package patterns.template;

import model.ShapeType;

/** Triangle inscribed in the drag bounding box. */
public class TriangleShape extends PolygonShape {

    public TriangleShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.TRIANGLE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        if (endY < startY) {
            // Triangle pointing downwards
            return new double[]{
                x + w / 2, y + h,  // apex center-bottom
                x,         y,      // top-left
                x + w,     y       // top-right
            };
        } else {
            // Triangle pointing upwards
            return new double[]{
                x + w / 2, y,      // apex center-top
                x,         y + h,  // bottom-left
                x + w,     y + h   // bottom-right
            };
        }
    }
}
