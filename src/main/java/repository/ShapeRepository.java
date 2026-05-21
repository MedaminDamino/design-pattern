package repository;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ShapeRepository – DAO / Repository Pattern.
 * Single Responsibility: CRUD for the shapes, graph_nodes, and graph_edges tables.
 */
public class ShapeRepository implements IShapeRepository {
    private final Connection conn;

    public ShapeRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void saveAll(int drawingId, List<DrawableShape> shapes) throws SQLException {
        // Clear previous entries for this drawing
        try (PreparedStatement del1 = conn.prepareStatement("DELETE FROM shapes WHERE drawing_id=?");
             PreparedStatement del2 = conn.prepareStatement("DELETE FROM graph_nodes WHERE drawing_id=?");
             PreparedStatement del3 = conn.prepareStatement("DELETE FROM graph_edges WHERE drawing_id=?")) {
            del1.setInt(1, drawingId); del1.executeUpdate();
            del2.setInt(1, drawingId); del2.executeUpdate();
            del3.setInt(1, drawingId); del3.executeUpdate();
        }
        
        String sqlShapes = "INSERT INTO shapes(drawing_id,type,start_x,start_y,end_x,end_y,width,height,radius,stroke_color,fill_color) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        String sqlNodes = "INSERT INTO graph_nodes(drawing_id,node_id,x,y,stroke_color,fill_color) VALUES(?,?,?,?,?,?)";
        String sqlEdges = "INSERT INTO graph_edges(drawing_id,start_x,start_y,end_x,end_y,weight,stroke_color,fill_color) VALUES(?,?,?,?,?,?,?,?)";

        try (PreparedStatement psShapes = conn.prepareStatement(sqlShapes);
             PreparedStatement psNodes = conn.prepareStatement(sqlNodes);
             PreparedStatement psEdges = conn.prepareStatement(sqlEdges)) {
            
            for (DrawableShape s : shapes) {
                if (s instanceof graph.GraphNode gn) {
                    psNodes.setInt(1, drawingId);
                    psNodes.setInt(2, gn.getNodeId());
                    // Undo offset added on instantiation
                    psNodes.setDouble(3, gn.getStartX());
                    psNodes.setDouble(4, gn.getStartY());
                    psNodes.setString(5, gn.getStrokeColor());
                    psNodes.setString(6, gn.getFillColor());
                    psNodes.addBatch();
                } else if (s instanceof graph.GraphEdge ge) {
                    psEdges.setInt(1, drawingId);
                    psEdges.setDouble(2, ge.getStartX());
                    psEdges.setDouble(3, ge.getStartY());
                    psEdges.setDouble(4, ge.getEndX());
                    psEdges.setDouble(5, ge.getEndY());
                    psEdges.setDouble(6, ge.getWeight());
                    psEdges.setString(7, ge.getStrokeColor());
                    psEdges.setString(8, ge.getFillColor());
                    psEdges.addBatch();
                } else {
                    psShapes.setInt(1, drawingId);
                    psShapes.setString(2, s.getType().name());
                    psShapes.setDouble(3, s.getStartX()); psShapes.setDouble(4, s.getStartY());
                    psShapes.setDouble(5, s.getEndX());   psShapes.setDouble(6, s.getEndY());
                    psShapes.setDouble(7, s.getWidth());  psShapes.setDouble(8, s.getHeight());
                    psShapes.setDouble(9, s.getRadius());
                    psShapes.setString(10, s.getStrokeColor());
                    psShapes.setString(11, s.getFillColor());
                    psShapes.addBatch();
                }
            }
            psShapes.executeBatch();
            psNodes.executeBatch();
            psEdges.executeBatch();
        }
    }

    public List<DrawableShape> findByDrawingId(int drawingId) throws SQLException {
        List<DrawableShape> list = new ArrayList<>();
        
        // 1. Load normal shapes
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM shapes WHERE drawing_id=?")) {
            ps.setInt(1, drawingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShapeType type = ShapeType.valueOf(rs.getString("type"));
                    double sx = rs.getDouble("start_x"), sy = rs.getDouble("start_y");
                    double ex = rs.getDouble("end_x"),   ey = rs.getDouble("end_y");
                    String stroke = rs.getString("stroke_color");
                    String fill   = rs.getString("fill_color");
                    
                    DrawableShape s = switch (type) {
                        case RECTANGLE -> new RectangleShape(sx, sy, ex, ey, stroke, fill);
                        case CIRCLE    -> new CircleShape(sx, sy, ex, ey, stroke, fill);
                        case LINE      -> new LineShape(sx, sy, ex, ey, stroke, fill);
                        case TRIANGLE  -> new TriangleShape(sx, sy, ex, ey, stroke, fill);
                        case ELLIPSE   -> new EllipseShape(sx, sy, ex, ey, stroke, fill);
                        case PENTAGON  -> new PentagonShape(sx, sy, ex, ey, stroke, fill);
                        case HEXAGON   -> new HexagonShape(sx, sy, ex, ey, stroke, fill);
                        case DIAMOND   -> new DiamondShape(sx, sy, ex, ey, stroke, fill);
                        case STAR      -> new StarShape(sx, sy, ex, ey, stroke, fill);
                        case ARROW     -> new ArrowShape(sx, sy, ex, ey, stroke, fill);
                        case TRAPEZOID -> new TrapezoidShape(sx, sy, ex, ey, stroke, fill);
                        default        -> null;
                    };
                    if (s != null) list.add(s);
                }
            }
        }
        
        // 2. Load Graph Nodes
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM graph_nodes WHERE drawing_id=?")) {
            ps.setInt(1, drawingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nodeId = rs.getInt("node_id");
                    double x = rs.getDouble("x"), y = rs.getDouble("y");
                    String stroke = rs.getString("stroke_color");
                    String fill   = rs.getString("fill_color");
                    
                    graph.GraphNode gn = new graph.GraphNode(nodeId, x + graph.GraphNode.NODE_RADIUS, y + graph.GraphNode.NODE_RADIUS, stroke, fill);
                    list.add(gn);
                }
            }
        }
        
        // 3. Load Graph Edges
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM graph_edges WHERE drawing_id=?")) {
            ps.setInt(1, drawingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double sx = rs.getDouble("start_x"), sy = rs.getDouble("start_y");
                    double ex = rs.getDouble("end_x"),   ey = rs.getDouble("end_y");
                    double weight = rs.getDouble("weight");
                    String stroke = rs.getString("stroke_color");
                    String fill   = rs.getString("fill_color");
                    
                    graph.GraphEdge ge = new graph.GraphEdge(sx, sy, ex, ey, stroke, fill);
                    ge.setWeight(weight);
                    list.add(ge);
                }
            }
        }
        
        return list;
    }
}
