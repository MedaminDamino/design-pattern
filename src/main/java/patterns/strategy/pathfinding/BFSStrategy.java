package patterns.strategy.pathfinding;

import graph.Graph;
import graph.GraphEdge;
import graph.GraphNode;
import java.util.*;

/**
 * BFSStrategy – Strategy Pattern concrete implementation.
 * Finds the fewest-hops path (unweighted) using BFS.
 */
public class BFSStrategy implements ShortestPathStrategy {

    @Override
    public List<GraphNode> findPath(Graph graph, GraphNode start, GraphNode end) {
        Queue<GraphNode> queue = new LinkedList<>();
        Map<GraphNode, GraphNode> prev = new HashMap<>();
        Set<GraphNode> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            GraphNode u = queue.poll();
            if (u == end) break;
            for (GraphEdge edge : graph.edgesFrom(u)) {
                GraphNode v = graph.getNeighbour(edge, u);
                if (!visited.contains(v)) {
                    visited.add(v);
                    prev.put(v, u);
                    queue.add(v);
                }
            }
        }

        List<GraphNode> path = new ArrayList<>();
        for (GraphNode at = end; at != null; at = prev.get(at)) path.add(0, at);
        return path.isEmpty() || path.get(0) != start ? Collections.emptyList() : path;
    }
}
