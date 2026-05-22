package patterns.command;

import graph.GraphEdge;

/**
 * UpdateEdgeWeightCommand – Command Pattern.
 * execute(): sets new weight on the edge.
 * undo():    restores the previous weight.
 */
public class UpdateEdgeWeightCommand implements Command {
    private final GraphEdge edge;
    private final double oldWeight;
    private final double newWeight;

    public UpdateEdgeWeightCommand(GraphEdge edge, double newWeight) {
        this.edge = edge;
        this.oldWeight = edge.getWeight();
        this.newWeight = newWeight;
    }

    @Override
    public void execute() {
        edge.setWeight(newWeight);
    }

    @Override
    public void undo() {
        edge.setWeight(oldWeight);
    }
}
