package command;

import model.Drawing;
import model.DrawableShape;
import javafx.scene.layout.Pane;

/**
 * AddShapeCommand – Command Pattern.
 * execute(): add shape to model + render on pane.
 * undo():    remove shape from model + erase from pane.
 */
public class AddShapeCommand implements Command {
    private final Drawing drawing;
    private final DrawableShape shape;
    private final Pane pane;

    public AddShapeCommand(Drawing drawing, DrawableShape shape, Pane pane) {
        this.drawing = drawing;
        this.shape   = shape;
        this.pane    = pane;
    }

    @Override
    public void execute() {
        shape.draw(pane);
        drawing.addShape(shape);
    }

    @Override
    public void undo() {
        shape.erase(pane);
        drawing.removeShape(shape);
    }

    public DrawableShape getShape() { return shape; }
}
