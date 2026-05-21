package service;

import command.Command;
import command.CommandManager;
import model.DrawableShape;
import model.Drawing;
import repository.IDrawingRepository;
import repository.IShapeRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * DrawingService - Handles business logic, abstracting it away from the UI controller.
 * SOLID: Single Responsibility Principle.
 */
public class DrawingService {
    private Drawing currentDrawing;
    private final CommandManager commandManager;

    public DrawingService() {
        this.commandManager = new CommandManager();
        this.currentDrawing = new Drawing("Untitled");
    }

    public Drawing getCurrentDrawing() {
        return currentDrawing;
    }

    public void setCurrentDrawing(Drawing currentDrawing) {
        this.currentDrawing = currentDrawing;
    }

    public void executeCommand(Command cmd) {
        commandManager.executeCommand(cmd);
    }

    public void undo() {
        commandManager.undo();
    }

    public void redo() {
        commandManager.redo();
    }

    public void clear() {
        currentDrawing.getShapes().clear();
        commandManager.clear();
    }

    /**
     * Resets the service to a brand-new, empty drawing named "Untitled".
     * Clears command history.
     */
    public void newDrawing() {
        currentDrawing = new Drawing("Untitled");
        commandManager.clear();
    }

    /**
     * Returns true when the current drawing has shapes but has never been
     * persisted (id == 0), signalling potential unsaved work.
     */
    public boolean hasUnsavedChanges() {
        return currentDrawing.getId() == 0 && !currentDrawing.getShapes().isEmpty();
    }

    public void clearCommandHistory() {
        commandManager.clear();
    }
}
