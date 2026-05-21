package graph;

import java.util.*;

/**
 * DijkstraStrategy – Strategy Pattern concrete implementation.
 * Finds the minimum-weight path between two nodes.
 */
public class DijkstraStrategy implements ShortestPathStrategy {

    @Override
    public List<GraphNode> findPath(Graph graph, GraphNode start, GraphNode end) {
        Map<GraphNode, Double> dist = new HashMap<>();
        Map<GraphNode, GraphNode> prev = new HashMap<>();
        PriorityQueue<GraphNode> pq = new PriorityQueue<>(
                Comparator.comparingDouble(n -> dist.getOrDefault(n, Double.MAX_VALUE)));

        for (GraphNode n : graph.getNodes()) dist.put(n, Double.MAX_VALUE);
        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {
            GraphNode u = pq.poll();
            if (u == end) break;
            for (GraphEdge edge : graph.edgesFrom(u)) {
                GraphNode v = graph.getNeighbour(edge, u);
                double alt = dist.get(u) + edge.getWeight();
                if (alt < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }
        return buildPath(prev, start, end);
    }

    private List<GraphNode> buildPath(Map<GraphNode, GraphNode> prev,
                                      GraphNode start, GraphNode end) {
        List<GraphNode> path = new ArrayList<>();
        for (GraphNode at = end; at != null; at = prev.get(at)) path.add(0, at);
        return path.isEmpty() || path.get(0) != start ? Collections.emptyList() : path;
    }
}
