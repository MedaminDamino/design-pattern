package patterns.strategy.storage;

import model.Drawing;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.layout.Pane;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * FileDrawingStorageStrategy – File-backed persistence strategy.
 * Prompts user for a location using native FileChooser, then uses
 * DrawingFileSerializer to load/save drawings.
 */
public class FileDrawingStorageStrategy implements DrawingStorageStrategy {

    @Override
    public boolean save(Drawing drawing, Pane shapePane, boolean saveAs) throws Exception {
        File file = null;
        if (saveAs || drawing.getFilePath() == null || drawing.getFilePath().isBlank()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(saveAs ? "Save Drawing As" : "Save Drawing to File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Drawing Files (*.drw)", "*.drw"));

            String name = drawing.getName();
            if (name == null || name.isBlank() || "Untitled".equalsIgnoreCase(name)) {
                fileChooser.setInitialFileName("drawing.drw");
            } else {
                fileChooser.setInitialFileName(name + ".drw");
            }

            Window window = shapePane.getScene().getWindow();
            file = fileChooser.showSaveDialog(window);
            if (file == null) {
                return false;
            }

            drawing.setFilePath(file.getAbsolutePath());
            String baseName = file.getName();
            if (baseName.endsWith(".drw")) {
                baseName = baseName.substring(0, baseName.length() - 4);
            }
            drawing.setName(baseName);
        } else {
            file = new File(drawing.getFilePath());
        }

        DrawingFileSerializer.serialize(file, drawing);
        return true;
    }

    @Override
    public List<Drawing> listSaved() throws Exception {
        return Collections.emptyList();
    }

    @Override
    public LoadedDrawing load(Drawing selectedDrawing, Pane shapePane) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Drawing File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Drawing Files (*.drw)", "*.drw"));

        Window window = shapePane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            return DrawingFileSerializer.deserialize(file);
        }
        return null;
    }

    @Override
    public String getModeName() {
        return "File";
    }
}
