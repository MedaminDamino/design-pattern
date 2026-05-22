package patterns.template;

import model.ShapeType;

/** Diamond (rhombus) – 4 points at midpoints of bounding-box edges. */
public class DiamondShape extends PolygonShape {

    public DiamondShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.DIAMOND, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        double cx = x + w / 2, cy = y + h / 2;
        return new double[]{
            cx,     y,          // top
            x + w,  cy,         // right
            cx,     y + h,      // bottom
            x,      cy          // left
        };
    }
}
