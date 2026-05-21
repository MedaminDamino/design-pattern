package storage;

import model.Drawing;
import model.DrawableShape;
import repository.IDrawingRepository;
import repository.IShapeRepository;
import javafx.scene.layout.Pane;
import java.util.List;

/**
 * DatabaseDrawingStorageStrategy – SQL-backed persistence strategy.
 * Delegates saving and loading to DrawingRepository and ShapeRepository.
 */
public class DatabaseDrawingStorageStrategy implements DrawingStorageStrategy {
    private final IDrawingRepository drawingRepository;
    private final IShapeRepository shapeRepository;

    public DatabaseDrawingStorageStrategy(IDrawingRepository drawingRepo, IShapeRepository shapeRepo) {
        this.drawingRepository = drawingRepo;
        this.shapeRepository = shapeRepo;
    }

    @Override
    public boolean save(Drawing drawing, Pane shapePane, boolean saveAs) throws Exception {
        if (saveAs) {
            drawing.setId(0);
        }
        if (drawing.getId() == 0) {
            drawingRepository.save(drawing);
        }
        shapeRepository.saveAll(drawing.getId(), drawing.getShapes());
        return true;
    }

    @Override
    public List<Drawing> listSaved() throws Exception {
        return drawingRepository.findAll();
    }

    @Override
    public LoadedDrawing load(Drawing selectedDrawing, Pane shapePane) throws Exception {
        if (selectedDrawing == null) {
            throw new IllegalArgumentException("No drawing selected to load from database.");
        }
        List<DrawableShape> shapes = shapeRepository.findByDrawingId(selectedDrawing.getId());
        selectedDrawing.getShapes().clear();
        for (DrawableShape s : shapes) {
            selectedDrawing.addShape(s);
        }
        return new LoadedDrawing(selectedDrawing, shapes);
    }

    @Override
    public String getModeName() {
        return "Database";
    }
}
