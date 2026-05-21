package storage;

import model.DrawableShape;
import model.Drawing;
import graph.GraphNode;
import graph.GraphEdge;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DrawingFileSerializer – Utility class for serialization and deserialization
 * of drawings to and from the custom .drw text format.
 * No JavaFX dependencies here – purely data serialization/deserialization.
 */
public class DrawingFileSerializer {

    public static void serialize(File file, Drawing drawing) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            writer.println("DRAWING_NAME=" + drawing.getName());
            
            for (DrawableShape shape : drawing.getShapes()) {
                if (shape instanceof GraphNode gn) {
                    writer.printf(Locale.US, "NODE=%d|%.6f|%.6f|%s|%s%n",
                            gn.getNodeId(),
                            gn.getCenterX(),
                            gn.getCenterY(),
                            gn.getStrokeColor(),
                            gn.getFillColor());
                } else if (shape instanceof GraphEdge ge) {
                    writer.printf(Locale.US, "EDGE=%.6f|%.6f|%.6f|%.6f|%.6f|%s|%s%n",
                            ge.getStartX(),
                            ge.getStartY(),
                            ge.getEndX(),
                            ge.getEndY(),
                            ge.getWeight(),
                            ge.getStrokeColor(),
                            ge.getFillColor());
                } else {
                    writer.printf(Locale.US, "SHAPE=%s|%.6f|%.6f|%.6f|%.6f|%s|%s%n",
                            shape.getType().name(),
                            shape.getStartX(),
                            shape.getStartY(),
                            shape.getEndX(),
                            shape.getEndY(),
                            shape.getStrokeColor(),
                            shape.getFillColor());
                }
            }
        }
    }

    private static double parseDouble(String str) throws NumberFormatException {
        if (str == null) {
            throw new NumberFormatException("Null string");
        }
        return Double.parseDouble(str.replace(',', '.').trim());
    }

    public static LoadedDrawing deserialize(File file) throws IOException {
        String drawingName = "Untitled";
        List<DrawableShape> shapes = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                if (line.startsWith("DRAWING_NAME=")) {
                    drawingName = line.substring("DRAWING_NAME=".length()).trim();
                } else if (line.startsWith("NODE=")) {
                    String partsStr = line.substring("NODE=".length());
                    String[] parts = partsStr.split("\\|");
                    if (parts.length >= 5) {
                        try {
                            int nodeId = Integer.parseInt(parts[0]);
                            double cx = parseDouble(parts[1]);
                            double cy = parseDouble(parts[2]);
                            String stroke = parts[3];
                            String fill = parts[4];
                            // Re-instantiate GraphNode using cx and cy
                            GraphNode gn = new GraphNode(nodeId, cx, cy, stroke, fill);
                            shapes.add(gn);
                        } catch (NumberFormatException e) {
                            // Skip malformed lines
                        }
                    }
                } else if (line.startsWith("EDGE=")) {
                    String partsStr = line.substring("EDGE=".length());
                    String[] parts = partsStr.split("\\|");
                    if (parts.length >= 7) {
                        try {
                            double sx = parseDouble(parts[0]);
                            double sy = parseDouble(parts[1]);
                            double ex = parseDouble(parts[2]);
                            double ey = parseDouble(parts[3]);
                            double weight = parseDouble(parts[4]);
                            String stroke = parts[5];
                            String fill = parts[6];
                            GraphEdge ge = new GraphEdge(sx, sy, ex, ey, stroke, fill);
                            ge.setWeight(weight);
                            shapes.add(ge);
                        } catch (NumberFormatException e) {
                            // Skip malformed lines
                        }
                    }
                } else if (line.startsWith("SHAPE=")) {
                    String partsStr = line.substring("SHAPE=".length());
                    String[] parts = partsStr.split("\\|");
                    if (parts.length >= 7) {
                        try {
                            model.ShapeType type = model.ShapeType.valueOf(parts[0]);
                            double sx = parseDouble(parts[1]);
                            double sy = parseDouble(parts[2]);
                            double ex = parseDouble(parts[3]);
                            double ey = parseDouble(parts[4]);
                            String stroke = parts[5];
                            String fill = parts[6];
                            
                            DrawableShape s = switch (type) {
                                case RECTANGLE -> new model.RectangleShape(sx, sy, ex, ey, stroke, fill);
                                case CIRCLE    -> new model.CircleShape(sx, sy, ex, ey, stroke, fill);
                                case LINE      -> new model.LineShape(sx, sy, ex, ey, stroke, fill);
                                case TRIANGLE  -> new model.TriangleShape(sx, sy, ex, ey, stroke, fill);
                                case ELLIPSE   -> new model.EllipseShape(sx, sy, ex, ey, stroke, fill);
                                case PENTAGON  -> new model.PentagonShape(sx, sy, ex, ey, stroke, fill);
                                case HEXAGON   -> new model.HexagonShape(sx, sy, ex, ey, stroke, fill);
                                case DIAMOND   -> new model.DiamondShape(sx, sy, ex, ey, stroke, fill);
                                case STAR      -> new model.StarShape(sx, sy, ex, ey, stroke, fill);
                                case ARROW     -> new model.ArrowShape(sx, sy, ex, ey, stroke, fill);
                                case TRAPEZOID -> new model.TrapezoidShape(sx, sy, ex, ey, stroke, fill);
                                default        -> null;
                            };
                            if (s != null) {
                                shapes.add(s);
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip malformed lines
                        }
                    }
                }
            }
        }
        
        Drawing drawing = new Drawing(drawingName);
        drawing.setFilePath(file.getAbsolutePath());
        for (DrawableShape s : shapes) {
            drawing.addShape(s);
        }
        return new LoadedDrawing(drawing, shapes);
    }
}
