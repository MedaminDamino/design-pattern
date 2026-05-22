package patterns.command;

import model.Drawing;
import model.DrawableShape;
import javafx.scene.layout.Pane;

/**
 * DeleteShapeCommand – Command Pattern.
 * execute(): remove shape from model + erase from pane.
 * undo():    re-add shape to model + redraw on pane.
 */
public class DeleteShapeCommand implements Command {
    private final Drawing drawing;
    private final DrawableShape shape;
    private final Pane pane;
    private final service.GraphService graphService;
    private java.util.List<graph.GraphEdge> removedEdges;

    public DeleteShapeCommand(Drawing drawing, DrawableShape shape, Pane pane, service.GraphService graphService) {
        this.drawing = drawing;
        this.shape   = shape;
        this.pane    = pane;
        this.graphService = graphService;
    }

    @Override
    public void execute() {
        shape.erase(pane);
        drawing.removeShape(shape);
        if (graphService != null) {
            if (shape instanceof graph.GraphNode node) {
                removedEdges = graphService.getGraph().edgesFrom(node);
                graphService.removeNode(node);
            } else if (shape instanceof graph.GraphEdge edge) {
                graphService.getGraph().removeEdge(edge);
            }
        }
    }

    @Override
    public void undo() {
        shape.draw(pane);
        drawing.addShape(shape);
        if (graphService != null) {
            if (shape instanceof graph.GraphNode node) {
                graphService.addNode(node);
                if (removedEdges != null) {
                    for (graph.GraphEdge e : removedEdges) {
                        e.draw(pane);
                        drawing.addShape(e);
                        graphService.getGraph().addEdge(e);
                    }
                }
            } else if (shape instanceof graph.GraphEdge edge) {
                graphService.getGraph().addEdge(edge);
            }
        }
    }
}
