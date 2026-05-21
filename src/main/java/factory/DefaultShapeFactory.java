package factory;

import model.*;
import graph.GraphEdge;
import graph.GraphNode;

/**
 * DefaultShapeFactory – concrete Factory Method implementation.
 * Open/Closed: adding a new shape = implement DrawableShape + add a case here.
 * Nothing else in the architecture changes.
 */
public class DefaultShapeFactory implements ShapeFactory {

    @Override
    public DrawableShape createShape(ShapeType type,
                                     double sx, double sy,
                                     double ex, double ey,
                                     String stroke, String fill) {
        return switch (type) {
            // ── Basic ──────────────────────────────────────────
            case RECTANGLE -> new RectangleShape(sx, sy, ex, ey, stroke, fill);
            case CIRCLE    -> new CircleShape(sx, sy, ex, ey, stroke, fill);
            case LINE      -> new LineShape(sx, sy, ex, ey, stroke, fill);
            // ── Polygons ───────────────────────────────────────
            case TRIANGLE  -> new TriangleShape(sx, sy, ex, ey, stroke, fill);
            case ELLIPSE   -> new EllipseShape(sx, sy, ex, ey, stroke, fill);
            case PENTAGON  -> new PentagonShape(sx, sy, ex, ey, stroke, fill);
            case HEXAGON   -> new HexagonShape(sx, sy, ex, ey, stroke, fill);
            case DIAMOND   -> new DiamondShape(sx, sy, ex, ey, stroke, fill);
            case STAR      -> new StarShape(sx, sy, ex, ey, stroke, fill);
            case ARROW     -> new ArrowShape(sx, sy, ex, ey, stroke, fill);
            case TRAPEZOID -> new TrapezoidShape(sx, sy, ex, ey, stroke, fill);
            // ── Graph ──────────────────────────────────────────
            case NODE      -> new GraphNode(0, sx, sy, stroke, fill);
            case EDGE      -> new GraphEdge(sx, sy, ex, ey, stroke, fill);
            // ── Tools ──────────────────────────────────────────
            case FILL      -> null;
        };
    }
}
