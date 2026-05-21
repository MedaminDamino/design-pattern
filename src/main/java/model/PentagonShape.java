package model;

/** Regular pentagon inscribed in bounding box. */
public class PentagonShape extends PolygonShape {

    public PentagonShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.PENTAGON, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        double cx = x + w / 2, cy = y + h / 2;
        double rx = w / 2, ry = h / 2;
        int sides = 5;
        double[] pts = new double[sides * 2];
        for (int i = 0; i < sides; i++) {
            double angle = Math.toRadians(-90 + 360.0 * i / sides);
            pts[i * 2]     = cx + rx * Math.cos(angle);
            pts[i * 2 + 1] = cy + ry * Math.sin(angle);
        }
        return pts;
    }
}
