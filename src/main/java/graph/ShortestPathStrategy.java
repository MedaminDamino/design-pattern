package graph;

import java.util.List;

/**
 * ShortestPathStrategy – Strategy Pattern interface.
 * Dependency Inversion: controller depends on this abstraction.
 */
public interface ShortestPathStrategy {
    /** Returns the ordered list of nodes on the shortest path, or empty if none. */
    List<GraphNode> findPath(Graph graph, GraphNode start, GraphNode end);
}
