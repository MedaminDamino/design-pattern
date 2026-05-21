package model;

/** Triangle inscribed in the drag bounding box. */
public class TriangleShape extends PolygonShape {

    public TriangleShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.TRIANGLE, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        // apex center-top, bottom-left, bottom-right
        return new double[]{
            x + w / 2, y,
            x,         y + h,
            x + w,     y + h
        };
    }
}
