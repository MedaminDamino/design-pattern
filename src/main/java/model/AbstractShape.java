package model;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * AbstractShape – Template Method base for all shapes.
 * Single Responsibility: stores common data fields only.
 * Subclasses implement draw() with specific JavaFX nodes.
 */
public abstract class AbstractShape implements DrawableShape {
    protected String id;
    protected ShapeType type;
    protected double startX, startY, endX, endY;
    protected double width, height, radius;
    protected String strokeColor;
    protected String fillColor;
    protected Node visualNode;

    public AbstractShape(ShapeType type, double sx, double sy, double ex, double ey,
                         String stroke, String fill) {
        this.type = type;
        this.startX = sx; this.startY = sy;
        this.endX = ex;   this.endY = ey;
        this.width  = Math.abs(ex - sx);
        this.height = Math.abs(ey - sy);
        this.radius = Math.min(width, height) / 2.0;
        this.strokeColor = (stroke == null || stroke.isBlank()) ? "#ffffff" : stroke;
        this.fillColor   = (fill   == null || fill.isBlank())   ? "none"    : fill;
    }

    @Override public String getId()            { return id; }
    @Override public void   setId(String id)   { this.id = id; }
    @Override public ShapeType getType()       { return type; }
    @Override public double getStartX()        { return startX; }
    @Override public double getStartY()        { return startY; }
    @Override public double getEndX()          { return endX; }
    @Override public double getEndY()          { return endY; }
    @Override public double getWidth()         { return width; }
    @Override public double getHeight()        { return height; }
    @Override public double getRadius()        { return radius; }
    @Override public String getStrokeColor()   { return strokeColor; }
    @Override public String getFillColor()     { return fillColor; }
    @Override public void   setStrokeColor(String c) { this.strokeColor = c; }
    @Override public void   setFillColor(String c)   { this.fillColor = c; }
    @Override public Node   getVisualNode()    { return visualNode; }

    @Override
    public void erase(Pane pane) {
        if (visualNode != null) pane.getChildren().remove(visualNode);
    }

    @Override
    public void move(double dx, double dy) {
        this.startX += dx;
        this.startY += dy;
        this.endX += dx;
        this.endY += dy;
        if (visualNode != null) {
            visualNode.setLayoutX(visualNode.getLayoutX() + dx);
            visualNode.setLayoutY(visualNode.getLayoutY() + dy);
        }
    }
}
