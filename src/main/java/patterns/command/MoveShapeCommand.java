package patterns.command;

import model.DrawableShape;
import model.Drawing;
import graph.GraphNode;
import graph.GraphEdge;
import service.GraphService;

/**
 * MoveShapeCommand – Command Pattern.
 * execute(): moves shape by (dx,dy); also refreshes connected graph edges.
 * undo():    moves shape back by (-dx,-dy).
 */
public class MoveShapeCommand implements Command {
    private final Drawing drawing;
    private final DrawableShape shape;
    private final double dx;
    private final double dy;
    private final GraphService graphService;

    public MoveShapeCommand(Drawing drawing, DrawableShape shape, double dx, double dy, GraphService graphService) {
        this.drawing = drawing;
        this.shape = shape;
        this.dx = dx;
        this.dy = dy;
        this.graphService = graphService;
    }

    @Override
    public void execute() {
        shape.move(dx, dy);
        updateEdgesIfNode(shape);
    }

    @Override
    public void undo() {
        shape.move(-dx, -dy);
        updateEdgesIfNode(shape);
    }

    private void updateEdgesIfNode(DrawableShape s) {
        if (s instanceof GraphNode node && graphService != null && graphService.getGraph() != null) {
            for (GraphEdge edge : graphService.getGraph().getEdges()) {
                if (edge.getSource() == node || edge.getTarget() == node) {
                    edge.updateCoordinates();
                }
            }
        }
    }
}
