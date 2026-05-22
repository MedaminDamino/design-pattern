package patterns.command;

import model.DrawableShape;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * FillShapeCommand – Command Pattern implementation for the Fill tool.
 * execute(): applies newFill color to the shape.
 * undo():    restores the previous fill color.
 */
public class FillShapeCommand implements Command {
    private final DrawableShape shape;
    private final String newFill;
    private final String oldFill;

    public FillShapeCommand(DrawableShape shape, String newFill, String oldFill) {
        this.shape = shape;
        this.newFill = newFill;
        this.oldFill = oldFill;
    }

    @Override
    public void execute() {
        shape.setFillColor(newFill);
        updateVisual(newFill);
    }

    @Override
    public void undo() {
        shape.setFillColor(oldFill);
        updateVisual(oldFill);
    }

    private void updateVisual(String color) {
        Node visual = shape.getVisualNode();
        if (visual instanceof javafx.scene.shape.Shape fxShape) {
            if ("none".equalsIgnoreCase(color) || "transparent".equalsIgnoreCase(color)) {
                fxShape.setFill(Color.TRANSPARENT);
            } else {
                try {
                    fxShape.setFill(Color.web(color));
                } catch (Exception e) {
                    fxShape.setFill(Color.WHITE);
                }
            }
        } else if (visual instanceof javafx.scene.Group grp && !grp.getChildren().isEmpty()) {
            // Support for GraphNode which is a Group containing a Circle
            if (grp.getChildren().get(0) instanceof javafx.scene.shape.Shape fxShape) {
                if ("none".equalsIgnoreCase(color) || "transparent".equalsIgnoreCase(color)) {
                    fxShape.setFill(Color.TRANSPARENT);
                } else {
                    try {
                        fxShape.setFill(Color.web(color));
                    } catch (Exception e) {
                        fxShape.setFill(Color.WHITE);
                    }
                }
            }
        }
    }
}
