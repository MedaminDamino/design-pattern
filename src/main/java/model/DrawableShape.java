package model;

import javafx.scene.layout.Pane;

/**
 * DrawableShape – Interface Segregation (ISP).
 * Every shape the user can draw must implement this contract.
 * Liskov Substitution: all concrete shapes are fully substitutable.
 */
public interface DrawableShape {
    String getId();
    void setId(String id);
    ShapeType getType();

    double getStartX();
    double getStartY();
    double getEndX();
    double getEndY();
    double getWidth();
    double getHeight();
    double getRadius();
    String getStrokeColor();
    String getFillColor();
    void setStrokeColor(String color);
    void setFillColor(String color);

    /** Renders this shape onto the given Pane. */
    void draw(Pane pane);

    /** Removes this shape's visual node from the Pane. */
    void erase(Pane pane);

    /** Returns the JavaFX node created by draw(). */
    javafx.scene.Node getVisualNode();

    /** Moves the shape by dx, dy. */
    void move(double dx, double dy);
}
