package graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph – adjacency list representation.
 * Single Responsibility: graph topology only.
 */
public class Graph {
    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public void addNode(GraphNode node) { nodes.add(node); }
    public void removeNode(GraphNode node) {
        nodes.remove(node);
        edges.removeIf(e -> e.getSource() == node || e.getTarget() == node);
    }

    public void addEdge(GraphEdge edge) { edges.add(edge); }
    public void removeEdge(GraphEdge edge) { edges.remove(edge); }

    public List<GraphNode> getNodes() { return nodes; }
    public List<GraphEdge> getEdges() { return edges; }

    public List<GraphEdge> edgesFrom(GraphNode node) {
        return edges.stream()
                .filter(e -> e.getSource() == node || e.getTarget() == node)
                .toList();
    }

    public GraphNode getNeighbour(GraphEdge edge, GraphNode from) {
        return edge.getSource() == from ? edge.getTarget() : edge.getSource();
    }

    public void clear() { nodes.clear(); edges.clear(); }

    public int getNextAvailableNodeId() {
        java.util.Set<Integer> usedIds = nodes.stream()
            .map(GraphNode::getNodeId)
            .collect(java.util.stream.Collectors.toSet());

        int id = 1;
        while (usedIds.contains(id)) {
            id++;
        }
        return id;
    }

    public GraphEdge findEdgeBetween(GraphNode a, GraphNode b) {
        return edges.stream()
            .filter(e -> (e.getSource() == a && e.getTarget() == b) ||
                         (e.getSource() == b && e.getTarget() == a))
            .findFirst()
            .orElse(null);
    }
}
