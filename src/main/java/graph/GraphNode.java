package graph;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.AbstractShape;
import model.ShapeType;

/**
 * GraphNode – a vertex in the graph, also a DrawableShape.
 * Liskov Substitution: fully substitutable as DrawableShape.
 * Visually redesigned to match classic Windows 95 flat aesthetic.
 */
public class GraphNode extends AbstractShape {
    public static final double NODE_RADIUS = 22;
    private final String label;
    private final int nodeId;

    public GraphNode(int id, double cx, double cy, String stroke, String fill) {
        super(ShapeType.NODE, cx - NODE_RADIUS, cy - NODE_RADIUS,
              cx + NODE_RADIUS, cy + NODE_RADIUS, stroke, fill);
        this.nodeId = id;
        this.label  = String.valueOf(nodeId);
        this.radius = NODE_RADIUS;
    }

    public int getNodeId() { return nodeId; }
    public String getLabel() { return label; }

    public double getCenterX() { return (startX + endX) / 2.0; }
    public double getCenterY() { return (startY + endY) / 2.0; }

    @Override
    public void draw(Pane pane) {
        Circle c = buildCircle(false);
        Text t = new Text(label);
        t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 11));
        t.setFill(Color.BLACK);
        // Center text in circle
        t.setX(getCenterX() - t.getLayoutBounds().getWidth() / 2);
        t.setY(getCenterY() + t.getLayoutBounds().getHeight() / 4);

        Group g = new Group(c, t);
        g.setUserData(this);
        pane.getChildren().add(g);
        this.visualNode = g;
    }

    /** Highlight this node during path visualization. */
    public void highlight(boolean on) {
        if (visualNode instanceof Group grp && !grp.getChildren().isEmpty()) {
            Circle c = (Circle) grp.getChildren().get(0);
            Text t = (Text) grp.getChildren().get(1);
            if (on) {
                c.setFill(Color.web("#000080")); // Classic win-blue
                c.setStroke(Color.BLACK);
                t.setFill(Color.WHITE);
            } else {
                c.setFill(Color.WHITE);
                c.setStroke(Color.BLACK);
                t.setFill(Color.BLACK);
            }
        }
    }

    private Circle buildCircle(boolean highlighted) {
        Circle c = new Circle(getCenterX(), getCenterY(), NODE_RADIUS);
        c.setFill(highlighted ? Color.web("#000080") : Color.WHITE);
        c.setStroke(Color.BLACK);
        c.setStrokeWidth(1);
        return c;
    }
}
