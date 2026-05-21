package service;

import command.AddShapeCommand;
import command.CommandManager;
import graph.Graph;
import graph.GraphEdge;
import graph.GraphNode;
import javafx.scene.layout.Pane;
import logging.LoggerContext;
import model.Drawing;

import java.util.List;

public class GraphService {
    private final Graph graph = new Graph();
    private final DrawingService drawingService;

    public GraphService(DrawingService drawingService) {
        this.drawingService = drawingService;
    }

    public Graph getGraph() { return graph; }

    public void clear() {
        graph.clear();
    }

    public void addNode(GraphNode node) {
        graph.addNode(node);
    }
    
    public void removeNode(GraphNode node) {
        // We need to properly clean up edges from drawing and UI
        List<GraphEdge> relatedEdges = graph.edgesFrom(node);
        for (GraphEdge e : relatedEdges) {
            graph.removeEdge(e);
            drawingService.getCurrentDrawing().removeShape(e);
            if (e.getVisualNode() != null && e.getVisualNode().getParent() instanceof Pane p) {
                e.erase(p);
            }
        }
        graph.getNodes().remove(node);
    }

    public void handleEdgeCreationOrUpdate(GraphNode source, GraphNode target, double weight, Pane pane) {
        GraphEdge existing = graph.findEdgeBetween(source, target);
        if (existing != null) {
            command.UpdateEdgeWeightCommand cmd = new command.UpdateEdgeWeightCommand(existing, weight);
            drawingService.executeCommand(cmd);
        } else {
            GraphEdge edge = new GraphEdge(source, target, weight);
            drawingService.executeCommand(new command.AddGraphEdgeCommand(drawingService.getCurrentDrawing(), edge, pane, this));
        }
    }
}
