package patterns.factory;

import model.DrawableShape;
import model.ShapeType;

/**
 * ShapeFactory – Factory Method Pattern (ISP).
 * Decouples shape creation from the controller.
 * Open/Closed: add shapes by creating a new concrete factory or extending this.
 */
public interface ShapeFactory {
    DrawableShape createShape(ShapeType type,
                              double startX, double startY,
                              double endX,   double endY,
                              String strokeColor, String fillColor);
}
