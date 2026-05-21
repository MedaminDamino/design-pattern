package model;

/** 5-pointed star inscribed in bounding box. */
public class StarShape extends PolygonShape {

    public StarShape(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.STAR, sx, sy, ex, ey, stroke, fill);
    }

    @Override
    protected double[] generatePoints(double x, double y, double w, double h) {
        double cx = x + w / 2, cy = y + h / 2;
        double outerRx = w / 2, outerRy = h / 2;
        double innerRx = outerRx * 0.4, innerRy = outerRy * 0.4;
        int spikes = 5;
        double[] pts = new double[spikes * 4];
        for (int i = 0; i < spikes; i++) {
            // outer point
            double outerAngle = Math.toRadians(-90 + 360.0 * i / spikes);
            pts[i * 4]     = cx + outerRx * Math.cos(outerAngle);
            pts[i * 4 + 1] = cy + outerRy * Math.sin(outerAngle);
            // inner point (between outer points)
            double innerAngle = Math.toRadians(-90 + 360.0 * i / spikes + 360.0 / (spikes * 2));
            pts[i * 4 + 2] = cx + innerRx * Math.cos(innerAngle);
            pts[i * 4 + 3] = cy + innerRy * Math.sin(innerAngle);
        }
        return pts;
    }
}
