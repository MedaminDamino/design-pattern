package model;

/** Trapezoid – wider at bottom, narrower at top (20% inset each side). */
public class TrapezoidShape extends PolygonShape {

    public TrapezoidShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.TRAPEZOID, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        double inset = w * 0.20;
        return new double[]{
            x + inset,      y,          // top-left
            x + w - inset,  y,          // top-right
            x + w,          y + h,      // bottom-right
            x,              y + h       // bottom-left
        };
    }
}
