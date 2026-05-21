package command;

import graph.GraphEdge;
import javafx.scene.layout.Pane;
import model.Drawing;
import service.GraphService;

public class AddGraphEdgeCommand implements Command {
    private final Drawing drawing;
    private final GraphEdge edge;
    private final Pane pane;
    private final GraphService graphService;

    public AddGraphEdgeCommand(Drawing drawing, GraphEdge edge, Pane pane, GraphService graphService) {
        this.drawing = drawing;
        this.edge = edge;
        this.pane = pane;
        this.graphService = graphService;
    }

    @Override
    public void execute() {
        edge.draw(pane);
        drawing.addShape(edge);
        graphService.getGraph().addEdge(edge);
    }

    @Override
    public void undo() {
        edge.erase(pane);
        drawing.removeShape(edge);
        graphService.getGraph().removeEdge(edge);
    }
}
