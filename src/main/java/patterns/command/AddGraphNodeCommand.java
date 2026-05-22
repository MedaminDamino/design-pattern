package patterns.command;

import graph.GraphNode;
import javafx.scene.layout.Pane;
import model.Drawing;
import service.GraphService;

/**
 * AddGraphNodeCommand – Command Pattern.
 * execute(): draws the node on the pane, adds to model and graph.
 * undo():    erases node, removes from model and graph (with its edges).
 */
public class AddGraphNodeCommand implements Command {
    private final Drawing drawing;
    private final GraphNode node;
    private final Pane pane;
    private final GraphService graphService;

    public AddGraphNodeCommand(Drawing drawing, GraphNode node, Pane pane, GraphService graphService) {
        this.drawing = drawing;
        this.node = node;
        this.pane = pane;
        this.graphService = graphService;
    }

    @Override
    public void execute() {
        node.draw(pane);
        drawing.addShape(node);
        graphService.addNode(node);
    }

    @Override
    public void undo() {
        node.erase(pane);
        drawing.removeShape(node);
        // GraphService handles erasing edges from Pane and Drawing in its removeNode
        graphService.removeNode(node);
    }
}
