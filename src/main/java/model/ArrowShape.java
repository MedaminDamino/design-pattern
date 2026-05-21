package model;

/** Right-pointing arrow shape. */
public class ArrowShape extends PolygonShape {

    public ArrowShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.ARROW, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        // Shaft occupies left 60%, arrowhead the right 40%
        double shaftH  = h * 0.40;         // shaft height (centered)
        double shaftY1 = y + (h - shaftH) / 2;
        double shaftY2 = shaftY1 + shaftH;
        double headX   = x + w * 0.60;     // where shaft ends / head begins

        return new double[]{
            x,          shaftY1,    // shaft top-left
            headX,      shaftY1,    // shaft top-right
            headX,      y,          // head top wing
            x + w,      y + h / 2, // arrow tip
            headX,      y + h,      // head bottom wing
            headX,      shaftY2,   // shaft bottom-right
            x,          shaftY2    // shaft bottom-left
        };
    }
}
