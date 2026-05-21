package graph;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.AbstractShape;
import model.ShapeType;

/**
 * GraphEdge – a weighted directed edge between two GraphNodes, also a DrawableShape.
 * Visually redesigned with styled line and readable weight badge.
 */
public class GraphEdge extends AbstractShape {
    private GraphNode source;
    private GraphNode target;
    private double weight;

    /** Used when loading from DB (no node references). */
    public GraphEdge(double sx, double sy, double ex, double ey, String stroke, String fill) {
        super(ShapeType.EDGE, sx, sy, ex, ey, stroke, fill);
        this.weight = 1;
    }

    public GraphEdge(GraphNode source, GraphNode target, double weight) {
        super(ShapeType.EDGE,
              source.getCenterX(), source.getCenterY(),
              target.getCenterX(), target.getCenterY(),
              "#6c72ff", "none");
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public GraphNode getSource() { return source; }
    public GraphNode getTarget() { return target; }
    public double    getWeight() { return weight; }

    public void setWeight(double weight) {
        this.weight = weight;
        if (visualNode instanceof Group grp && grp.getChildren().size() > 1) {
            if (grp.getChildren().get(1) instanceof Text text) {
                text.setText(formatWeight(weight));
            }
        }
    }

    @Override
    public void draw(Pane pane) {
        Line line = buildLine(false);

        double mx = (startX + endX) / 2.0;
        double my = (startY + endY) / 2.0;
        Text wt = new Text(formatWeight(weight));
        wt.setFont(Font.font("MS Sans Serif", FontWeight.NORMAL, 11));
        wt.setFill(Color.BLACK);
        // Offset badge slightly above midpoint
        wt.setX(mx + 6);
        wt.setY(my - 6);

        Group g = new Group(line, wt);
        pane.getChildren().add(0, g); // edges behind nodes
        this.visualNode = g;
    }

    /** Highlight edge during path visualization. */
    public void highlight(boolean on) {
        if (visualNode instanceof Group grp && !grp.getChildren().isEmpty()) {
            Line l = (Line) grp.getChildren().get(0);
            Text t = (Text) grp.getChildren().get(1);
            if (on) {
                l.setStroke(Color.web("#000080"));
                l.setStrokeWidth(2);
                t.setFill(Color.web("#000080"));
                t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 11));
            } else {
                l.setStroke(Color.BLACK);
                l.setStrokeWidth(1);
                t.setFill(Color.BLACK);
                t.setFont(Font.font("MS Sans Serif", FontWeight.NORMAL, 11));
            }
        }
    }

    private Line buildLine(boolean highlighted) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(highlighted ? Color.web("#000080") : Color.BLACK);
        line.setStrokeWidth(highlighted ? 2 : 1);
        line.setStrokeLineCap(StrokeLineCap.SQUARE);
        // Remove dashed style for retro look or keep it solid
        return line;
    }

    private String formatWeight(double w) {
        return w == Math.floor(w) ? String.valueOf((int) w) : String.format("%.1f", w);
    }

    public void bindToNodes(java.util.List<GraphNode> nodes) {
        this.source = nodes.stream()
            .filter(n -> Math.hypot(n.getCenterX() - startX, n.getCenterY() - startY) <= 5.0)
            .findFirst().orElse(null);
        this.target = nodes.stream()
            .filter(n -> Math.hypot(n.getCenterX() - endX, n.getCenterY() - endY) <= 5.0)
            .findFirst().orElse(null);
    }

    public void updateCoordinates() {
        if (source != null && target != null) {
            this.startX = source.getCenterX();
            this.startY = source.getCenterY();
            this.endX = target.getCenterX();
            this.endY = target.getCenterY();
            this.width = Math.abs(endX - startX);
            this.height = Math.abs(endY - startY);
            if (visualNode instanceof Group grp && !grp.getChildren().isEmpty()) {
                if (grp.getChildren().get(0) instanceof Line line) {
                    line.setStartX(this.startX);
                    line.setStartY(this.startY);
                    line.setEndX(this.endX);
                    line.setEndY(this.endY);
                }
                if (grp.getChildren().size() > 1 && grp.getChildren().get(1) instanceof Text wt) {
                    double mx = (this.startX + this.endX) / 2.0;
                    double my = (this.startY + this.endY) / 2.0;
                    wt.setX(mx + 6);
                    wt.setY(my - 6);
                }
            }
        }
    }
}
