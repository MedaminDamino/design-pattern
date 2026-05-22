package controller;

import command.*;
import factory.*;
import graph.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import logging.*;
import model.*;
import repository.*;
import service.DrawingService;
import storage.*;
import util.AlertUtil;
import util.OpenDrawingDialog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MainController – MVC Controller.
 * Single Responsibility: wires UI events to services and commands.
 * Dependency Inversion: depends on interfaces (ShapeFactory, ILogRepository,
 * etc.)
 * Controller stays thin – no business or DB logic here.
 */
public class MainController {

    // ── FXML bindings ────────────────────────────────────────────────────────
    @FXML
    private Pane shapePane;
    @FXML
    private TextField drawingNameField;

    @FXML
    private ToggleGroup shapeToggleGroup;
    @FXML
    private ToggleButton rectBtn, circleBtn, lineBtn, nodeBtn, edgeBtn;
    @FXML
    private ToggleButton triangleBtn, ellipseBtn, pentagonBtn, hexagonBtn;
    @FXML
    private ToggleButton diamondBtn, starBtn, fillBtn, arrowBtn, trapezoidBtn;
    @FXML
    private ToggleButton selectBtn;

    @FXML
    private ComboBox<String> logStrategyCombo;
    @FXML
    private ComboBox<String> storageCombo;
    @FXML
    private ComboBox<String> algorithmCombo;
    @FXML
    private ListView<String> actionLogView;

    @FXML
    private HBox colorGrid;
    @FXML
    private Rectangle activeStrokeRect;
    @FXML
    private Rectangle activeFillRect;

    private Color currentStrokeColor = Color.web("#2B2B2B"); // charcoal
    private Color currentFillColor   = Color.web("#FAF0E6"); // linen cream

    // Status bar
    @FXML
    private Label statusLabel;
    @FXML
    private Label shapeCountLabel;
    @FXML
    private Label modeLabel;

    // ── App state ─────────────────────────────────────────────────────────────
    private DrawingService drawingService;
    private final ShapeFactory shapeFactory = new DefaultShapeFactory();
    private final LoggerContext logger = LoggerContext.getInstance();
    private StorageContext storageContext;

    // Graph feature
    private service.GraphService graphService;
    private GraphNode pendingEdgeSource = null;

    // Repositories
    private IDrawingRepository drawingRepo;
    private IShapeRepository shapeRepo;
    private ILogRepository logRepo;

    // Drawing gesture state
    private double pressX, pressY;
    private Shape previewShape = null;
    private DrawableShape selectedShape = null;
    private Shape selectionBorder = null;

    private boolean isDraggingSelectedShape = false;
    private double lastDragX, lastDragY;
    private double dragTotalDX = 0, dragTotalDY = 0;

    // ── Initialisation ────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        boolean dbConnected = false;
        drawingService = new DrawingService();
        graphService = new service.GraphService(drawingService);

        try {
            drawingRepo = new DrawingRepository();
            shapeRepo = new ShapeRepository();
            logRepo = new LogRepository();
            storageContext = new StorageContext(new DatabaseDrawingStorageStrategy(drawingRepo, shapeRepo));
            dbConnected = true;
        } catch (Exception e) {
            System.err.println("Database connection failed. Falling back to File Storage mode. Error: " + e.getMessage());
            storageContext = new StorageContext(new FileDrawingStorageStrategy());
            
            // Show a friendly warning to the user asynchronously so it doesn't block JavaFX thread startup
            javafx.application.Platform.runLater(() -> {
                AlertUtil.showInfo("Database Offline",
                    "Could not connect to the MySQL database.\n\n" +
                    "The application will run in File Storage mode.\n" +
                    "To use database storage and logger, please start MySQL (e.g. via XAMPP) and restart the application.");
            });
        }

        // Default logger = console
        logger.setStrategy(new ConsoleLoggingStrategy());

        // Logging strategy combo
        if (dbConnected) {
            logStrategyCombo.getItems().addAll("Console", "File", "Database");
        } else {
            logStrategyCombo.getItems().addAll("Console", "File");
        }
        logStrategyCombo.setValue("Console");
        logStrategyCombo.setOnAction(e -> onLogStrategyChanged());

        // Storage strategy combo
        if (dbConnected) {
            storageCombo.getItems().addAll("Database", "File");
            storageCombo.setValue("Database");
        } else {
            storageCombo.getItems().addAll("File");
            storageCombo.setValue("File");
        }
        storageCombo.setOnAction(e -> onStorageStrategyChanged());

        // Graph algorithm combo
        algorithmCombo.getItems().addAll("Dijkstra", "BFS");
        algorithmCombo.setValue("Dijkstra");

        // Setup classic color palette
        setupColorPalette();

        // Listen to palette selection to update mode label
        shapeToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> updateModeLabel());

        // ── Win95 log cell factory ───────────────────────────────────────
        actionLogView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
            }
        });

        updateStatusBar();
        addToLog("App", "Drawing application started.");
    }

    // ── New Drawing ───────────────────────────────────────────────────────────

    @FXML
    private void onNewDrawing() {
        if (drawingService.hasUnsavedChanges()) {
            boolean proceed = AlertUtil.confirm("New Drawing",
                    "You have unsaved changes. Discard them and start a new drawing?");
            if (!proceed) {
                addToLog("CANCEL", "New drawing cancelled – unsaved changes kept.");
                return;
            }
        }
        clearCanvas();
        drawingService.newDrawing();
        graphService.clear();
        drawingNameField.setText("");
        actionLogView.getItems().clear();
        shapeToggleGroup.selectToggle(selectBtn);
        updateStatusBar();
        addToLog("NEW", "New drawing started.");
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    @FXML
    private void onSave() {
        saveDrawingWorkflow(false);
    }

    @FXML
    private void onSaveAs() {
        saveDrawingWorkflow(true);
    }

    private void saveDrawingWorkflow(boolean saveAs) {
        String name = drawingNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("Save", "Please enter a drawing name.");
            return;
        }
        try {
            Drawing drawing = drawingService.getCurrentDrawing();
            drawing.setName(name);
            boolean saved = storageContext.save(drawing, shapePane, saveAs);
            if (saved) {
                int count = drawing.getShapes().size();
                String actionType = saveAs ? "SAVE_AS" : "SAVE";
                addToLog(actionType, "Drawing '" + drawing.getName() + "' saved with " + count + " shapes.");
                setStatus("Saved: " + drawing.getName());
                drawingNameField.setText(drawing.getName());
                AlertUtil.showInfo("Saved", "Drawing '" + drawing.getName() + "' saved successfully.");
            } else {
                addToLog(saveAs ? "SAVE_AS_CANCEL" : "SAVE_CANCEL", "Save cancelled by user.");
            }
        } catch (Exception ex) {
            AlertUtil.showError("Save Error", ex.getMessage());
        }
    }

    // ── Open modal ────────────────────────────────────────────────────────────

    @FXML
    private void onOpen() {
        try {
            DrawingStorageStrategy strategy = storageContext.getStrategy();
            LoadedDrawing loaded = null;

            if ("Database".equals(strategy.getModeName())) {
                List<model.Drawing> allDrawings = strategy.listSaved();
                if (allDrawings.isEmpty()) {
                    AlertUtil.showInfo("Open", "No saved drawings exist in the database.");
                    return;
                }

                addToLog("OPEN_MODAL", "Open drawings modal displayed.");
                Optional<model.Drawing> chosen = OpenDrawingDialog.show(allDrawings);
                if (chosen.isEmpty()) {
                    return;
                }
                
                loaded = strategy.load(chosen.get(), shapePane);
            } else {
                // File mode
                loaded = strategy.load(null, shapePane);
                if (loaded == null) {
                    // User cancelled FileChooser
                    return;
                }
            }

            clearCanvas();
            drawingService.setCurrentDrawing(loaded.getDrawing());
            drawingService.clearCommandHistory();

            List<DrawableShape> shapes = loaded.getShapes();
            for (DrawableShape s : shapes) {
                s.draw(shapePane);
                if (s instanceof GraphNode gn) {
                    attachNodeClickHandler(gn);
                } else if (!(s instanceof GraphEdge)) {
                    attachClickHandler(s);
                }
            }

            // Restore graph and pathfinding state
            graphService.clear();
            for (DrawableShape s : shapes) {
                if (s instanceof GraphNode gn) {
                    graphService.addNode(gn);
                }
                if (s instanceof GraphEdge ge) {
                    graphService.getGraph().addEdge(ge);
                }
            }
            // Bind graph edge listeners for interactive moving
            for (GraphEdge edge : graphService.getGraph().getEdges()) {
                edge.bindToNodes(graphService.getGraph().getNodes());
            }

            // Reset selected tool to Select
            shapeToggleGroup.selectToggle(selectBtn);

            drawingNameField.setText(loaded.getDrawing().getName());
            updateStatusBar();
            
            String logAction = "Database".equals(strategy.getModeName()) ? "OPEN" : "OPEN_FILE";
            addToLog(logAction, "Opened drawing '" + loaded.getDrawing().getName() + "'");
            setStatus("Opened: " + loaded.getDrawing().getName());
        } catch (Exception ex) {
            AlertUtil.showError("Open Error", "Could not render drawing: " + ex.getMessage());
        }
    }

    // ── Menu Handlers ─────────────────────────────────────────────────────────

    @FXML
    private void onExit() {
        System.exit(0);
    }

    @FXML
    private void onMenuNode() {
        shapeToggleGroup.selectToggle(nodeBtn);
    }

    @FXML
    private void onMenuEdge() {
        shapeToggleGroup.selectToggle(edgeBtn);
    }

    @FXML
    private void onLoggerConsole() {
        if (!"Console".equals(logStrategyCombo.getValue())) {
            logStrategyCombo.setValue("Console");
        }
    }
 
    @FXML
    private void onLoggerFile() {
        if (!"File".equals(logStrategyCombo.getValue())) {
            logStrategyCombo.setValue("File");
        }
    }
 
    @FXML
    private void onLoggerDB() {
        if (logRepo == null) {
            AlertUtil.showError("Database Logger", "Database is not connected. Cannot use Database logger.");
            return;
        }
        if (!"Database".equals(logStrategyCombo.getValue())) {
            logStrategyCombo.setValue("Database");
        }
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    @FXML
    private void onUndo() {
        resetInteractionState();
        drawingService.undo();
        updateStatusBar();
        addToLog("UNDO", "Undo last action.");
    }

    @FXML
    private void onRedo() {
        resetInteractionState();
        drawingService.redo();
        updateStatusBar();
        addToLog("REDO", "Redo last action.");
    }

    // ── Delete / Clear ────────────────────────────────────────────────────────

    @FXML
    private void onDelete() {
        clearGraphHighlights();
        if (selectedShape == null) {
            AlertUtil.showError("Delete", "No shape selected. Click a shape first.");
            return;
        }
        removeSelectionBorder();
        DeleteShapeCommand cmd = new DeleteShapeCommand(
                drawingService.getCurrentDrawing(), selectedShape, shapePane, graphService);
        drawingService.executeCommand(cmd);
        addToLog("DELETE", "Deleted shape type=" + selectedShape.getType());
        selectedShape = null;
        updateStatusBar();
    }

    @FXML
    private void onClear() {
        clearGraphHighlights();
        if (!AlertUtil.confirm("Clear", "Clear all shapes from the canvas?"))
            return;
        clearCanvas();
        drawingService.clear();
        graphService.clear();
        updateStatusBar();
        addToLog("CLEAR", "Canvas cleared.");
    }

    // ── Canvas mouse handlers ─────────────────────────────────────────────────

    @FXML
    private void onCanvasMousePressed(MouseEvent e) {
        if (e.isConsumed())
            return;

        ShapeType type = getSelectedType();
        pressX = e.getX();
        pressY = e.getY();

        if (type == null) {
            // Select mode
            DrawableShape hitShape = null;
            for (int i = drawingService.getCurrentDrawing().getShapes().size() - 1; i >= 0; i--) {
                DrawableShape shape = drawingService.getCurrentDrawing().getShapes().get(i);
                Node visual = shape.getVisualNode();
                if (visual != null && visual.contains(visual.parentToLocal(pressX, pressY))) {
                    hitShape = shape;
                    break;
                }
            }
            if (hitShape != null) {
                selectShape(hitShape);
                isDraggingSelectedShape = true;
                lastDragX = pressX;
                lastDragY = pressY;
                dragTotalDX = 0;
                dragTotalDY = 0;
                e.consume();
            } else {
                removeSelectionBorder();
                selectedShape = null;
            }
            return;
        }

        // Deselect if clicking on empty canvas
        removeSelectionBorder();
        selectedShape = null;

        if (type == ShapeType.FILL) {
            handleFillClick(e);
            return;
        }
        if (type == ShapeType.NODE) {
            createNode(pressX, pressY);
            return;
        }
        if (type == ShapeType.EDGE) {
            handleEdgeClick(pressX, pressY);
            return;
        }

        previewShape = buildPreview(type, pressX, pressY, pressX + 1, pressY + 1);
        if (previewShape != null)
            shapePane.getChildren().add(previewShape);
    }

    private void handleFillClick(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        // Find top-most shape under cursor
        for (int i = drawingService.getCurrentDrawing().getShapes().size() - 1; i >= 0; i--) {
            DrawableShape shape = drawingService.getCurrentDrawing().getShapes().get(i);
            Node visual = shape.getVisualNode();
            if (visual != null && visual.contains(visual.parentToLocal(x, y))) {
                if (shape.getType() == ShapeType.NODE || shape.getType() == ShapeType.EDGE
                        || shape.getType() == ShapeType.LINE) {
                    continue; // Skip lines, nodes, edges
                }

                String newFill;
                if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    newFill = toHex(currentFillColor); // Right click applies secondary/background color
                } else {
                    newFill = toHex(currentStrokeColor); // Left click applies primary/foreground color
                }

                String oldFill = shape.getFillColor();
                FillShapeCommand cmd = new FillShapeCommand(shape, newFill, oldFill);
                drawingService.executeCommand(cmd);
                addToLog("FILL", "Filled " + shape.getType() + " with " + newFill);
                return;
            }
        }
    }

    @FXML
    private void onCanvasMouseDragged(MouseEvent e) {
        if (isDraggingSelectedShape && selectedShape != null) {
            double dx = e.getX() - lastDragX;
            double dy = e.getY() - lastDragY;

            selectedShape.move(dx, dy);
            dragTotalDX += dx;
            dragTotalDY += dy;

            lastDragX = e.getX();
            lastDragY = e.getY();

            if (selectionBorder != null) {
                selectionBorder.setLayoutX(selectionBorder.getLayoutX() + dx);
                selectionBorder.setLayoutY(selectionBorder.getLayoutY() + dy);
            }

            if (selectedShape instanceof GraphNode node) {
                for (GraphEdge edge : graphService.getGraph().getEdges()) {
                    if (edge.getSource() == node || edge.getTarget() == node) {
                        edge.updateCoordinates();
                    }
                }
            }
            return;
        }

        if (previewShape == null)
            return;
        shapePane.getChildren().remove(previewShape);
        previewShape = buildPreview(getSelectedType(), pressX, pressY, e.getX(), e.getY());
        if (previewShape != null) {
            shapePane.getChildren().add(previewShape);
        }
    }

    @FXML
    private void onCanvasMouseReleased(MouseEvent e) {
        if (isDraggingSelectedShape) {
            isDraggingSelectedShape = false;
            if (dragTotalDX != 0 || dragTotalDY != 0) {
                selectedShape.move(-dragTotalDX, -dragTotalDY);
                MoveShapeCommand cmd = new MoveShapeCommand(drawingService.getCurrentDrawing(), selectedShape,
                        dragTotalDX, dragTotalDY, graphService);
                drawingService.executeCommand(cmd);
                addToLog("MOVE", "Moved shape " + selectedShape.getType() + " by (" + fmt(dragTotalDX) + ", "
                        + fmt(dragTotalDY) + ")");

                // Keep the selection box properly aligned without accumulated layout offset
                selectShape(selectedShape);
            }
            return;
        }

        ShapeType type = getSelectedType();
        if (type == null || type == ShapeType.NODE || type == ShapeType.EDGE || type == ShapeType.FILL)
            return;

        double rawEx = e.getX();
        double rawEy = e.getY();
        double dx = rawEx - pressX;
        double dy = rawEy - pressY;

        double ex = rawEx;
        double ey = rawEy;

        // If the user just clicked without dragging (or dragged less than 5px),
        // we create a default-sized shape (100.0px) for visibility.
        // Otherwise, if they dragged, we use their exact mouse coordinates!
        if (Math.abs(dx) < 5.0 && Math.abs(dy) < 5.0) {
            ex = pressX + 100.0;
            ey = pressY + 100.0;
        }

        if (previewShape != null) {
            shapePane.getChildren().remove(previewShape);
            previewShape = null;
        }

        String stroke = toHex(currentStrokeColor);
        String fill = (type == ShapeType.LINE || type == ShapeType.ARROW) ? "none" : toHex(currentFillColor);

        DrawableShape shape = shapeFactory.createShape(type, pressX, pressY, ex, ey, stroke, fill);
        AddShapeCommand cmd = new AddShapeCommand(drawingService.getCurrentDrawing(), shape, shapePane);
        drawingService.executeCommand(cmd);
        attachClickHandler(shape);

        updateStatusBar();
        addToLog("CREATE", type + " from (" + fmt(pressX) + "," + fmt(pressY)
                + ") to (" + fmt(ex) + "," + fmt(ey) + ")");
    }

    // ── Graph helpers ─────────────────────────────────────────────────────────

    private void createNode(double x, double y) {
        clearGraphHighlights();
        int id = graphService.getGraph().getNextAvailableNodeId();
        GraphNode node = new GraphNode(id, x, y, "#6c72ff", "#6c72ff");
        AddGraphNodeCommand cmd = new AddGraphNodeCommand(
                drawingService.getCurrentDrawing(), node, shapePane, graphService);
        drawingService.executeCommand(cmd);
        attachNodeClickHandler(node);
        updateStatusBar();
        addToLog("CREATE", "GraphNode " + node.getLabel() + " at (" + fmt(x) + "," + fmt(y) + ")");
    }

    private void handleEdgeClick(double x, double y) {
        clearGraphHighlights();
        GraphNode hit = findNodeAt(x, y);
        if (hit == null)
            return;

        if (pendingEdgeSource == null) {
            pendingEdgeSource = hit;
            hit.highlight(true);
            addToLog("EDGE", "Selected source node " + hit.getLabel());
        } else {
            if (hit == pendingEdgeSource) {
                pendingEdgeSource.highlight(false);
                pendingEdgeSource = null;
                return;
            }
            Optional<String> wOpt = AlertUtil.prompt("Edge Weight", "Enter weight:", "1");
            if (wOpt.isEmpty()) {
                pendingEdgeSource.highlight(false);
                pendingEdgeSource = null;
                return;
            }
            double w = wOpt.map(s -> {
                try {
                    return Double.parseDouble(s);
                } catch (Exception ex) {
                    return 1.0;
                }
            }).orElse(1.0);
            pendingEdgeSource.highlight(false);

            boolean isUpdate = graphService.getGraph().findEdgeBetween(pendingEdgeSource, hit) != null;
            graphService.handleEdgeCreationOrUpdate(pendingEdgeSource, hit, w, shapePane);
            if (isUpdate) {
                addToLog("EDGE_UPDATE", pendingEdgeSource.getLabel() + " <-> " + hit.getLabel() + " (w=" + w + ")");
            } else {
                addToLog("EDGE", pendingEdgeSource.getLabel() + " <-> " + hit.getLabel() + " (w=" + w + ")");
            }
            pendingEdgeSource = null;
        }
    }

    @FXML
    private void onFindPath() {
        clearGraphHighlights();
        Graph graph = graphService.getGraph();
        if (graph.getNodes().size() < 2) {
            AlertUtil.showError("Path", "Need at least 2 graph nodes.");
            return;
        }
        List<String> labels = graph.getNodes().stream().map(GraphNode::getLabel).toList();
        Optional<String> s = showChoiceDialog("Path Start", "Select start node:", labels);
        if (s.isEmpty())
            return;
        Optional<String> t = showChoiceDialog("Path End", "Select end node:", labels);
        if (t.isEmpty())
            return;

        GraphNode start = findNodeByLabel(s.get());
        GraphNode end = findNodeByLabel(t.get());
        if (start == null || end == null || start == end) {
            AlertUtil.showError("Path", "Invalid nodes selected.");
            return;
        }

        ShortestPathStrategy strategy = "BFS".equals(algorithmCombo.getValue())
                ? new BFSStrategy()
                : new DijkstraStrategy();
        List<GraphNode> path = strategy.findPath(graph, start, end);

        if (path.isEmpty()) {
            AlertUtil.showInfo("Path", "No path found.");
            return;
        }

        for (GraphNode n : path)
            n.highlight(true);
        for (int i = 0; i < path.size() - 1; i++) {
            GraphNode u = path.get(i), v = path.get(i + 1);
            graph.getEdges().stream()
                    .filter(edge -> (edge.getSource() == u && edge.getTarget() == v)
                            || (edge.getSource() == v && edge.getTarget() == u))
                    .findFirst().ifPresent(edge -> edge.highlight(true));
        }
        addToLog("PATH", algorithmCombo.getValue() + " path: "
                + path.stream().map(GraphNode::getLabel).toList());
    }

    @FXML
    private void onClearPath() {
        clearGraphHighlights();
    }

    // ── Logging strategy swap ─────────────────────────────────────────────────

    private void onLogStrategyChanged() {
        String choice = logStrategyCombo.getValue();
        if (choice == null) return;
        if ("Database".equals(choice) && logRepo == null) {
            AlertUtil.showError("Database Logger", "Database is not connected. Cannot use Database logger.");
            logStrategyCombo.setValue("Console");
            return;
        }
        LoggingStrategy newStrategy = switch (choice) {
            case "File" -> new FileLoggingStrategy();
            case "Database" -> new DatabaseLoggingStrategy(logRepo);
            default -> new ConsoleLoggingStrategy();
        };
        logger.setStrategy(newStrategy);
        logger.log("CONFIG", "Logging strategy changed to: " + choice);
        addToLog("CONFIG", "Logger → " + choice);
    }

    private void onStorageStrategyChanged() {
        String choice = storageCombo.getValue();
        if (choice == null) return;
        if ("Database".equals(choice) && (drawingRepo == null || shapeRepo == null)) {
            AlertUtil.showError("Database Storage", "Database is not connected. Cannot use Database storage.");
            storageCombo.setValue("File");
            return;
        }
        DrawingStorageStrategy newStrategy = switch (choice) {
            case "File" -> new FileDrawingStorageStrategy();
            default -> new DatabaseDrawingStorageStrategy(drawingRepo, shapeRepo);
        };
        storageContext.setStrategy(newStrategy);
        addToLog("CONFIG", "Storage → " + choice);
    }

    // ── Selection highlight ───────────────────────────────────────────────────

    private void attachClickHandler(DrawableShape shape) {
        Node visual = shape.getVisualNode();
        if (visual == null)
            return;
        visual.setOnMouseClicked(e -> {
            selectShape(shape);
            e.consume();
        });
    }

    private void attachNodeClickHandler(GraphNode node) {
        Node visual = node.getVisualNode();
        if (visual == null)
            return;
        visual.setOnMouseClicked(e -> {
            ShapeType t = getSelectedType();
            if (t == ShapeType.EDGE)
                handleEdgeClick(node.getCenterX(), node.getCenterY());
            else
                selectShape(node);
            e.consume();
        });
    }

    private void selectShape(DrawableShape shape) {
        removeSelectionBorder();
        selectedShape = shape;
        Node v = shape.getVisualNode();

        // Build a generic bounding-rectangle selection indicator
        double minX, minY, w, h;
        if (v instanceof Rectangle r) {
            minX = r.getX() - 4;
            minY = r.getY() - 4;
            w = r.getWidth() + 8;
            h = r.getHeight() + 8;
        } else if (v instanceof Circle c) {
            minX = c.getCenterX() - c.getRadius() - 4;
            minY = c.getCenterY() - c.getRadius() - 4;
            w = h = (c.getRadius() + 4) * 2;
        } else if (v instanceof javafx.scene.shape.Ellipse el) {
            minX = el.getCenterX() - el.getRadiusX() - 4;
            minY = el.getCenterY() - el.getRadiusY() - 4;
            w = (el.getRadiusX() + 4) * 2;
            h = (el.getRadiusY() + 4) * 2;
        } else if (v instanceof Polygon poly) {
            double[] pts = poly.getPoints().stream().mapToDouble(Number::doubleValue).toArray();
            double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
            double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
            for (int i = 0; i < pts.length; i += 2) {
                xMin = Math.min(xMin, pts[i]);
                xMax = Math.max(xMax, pts[i]);
                yMin = Math.min(yMin, pts[i + 1]);
                yMax = Math.max(yMax, pts[i + 1]);
            }
            minX = xMin - 4;
            minY = yMin - 4;
            w = xMax - xMin + 8;
            h = yMax - yMin + 8;
        } else {
            return; // Line or Group – skip border
        }

        Rectangle border = new Rectangle(minX + v.getLayoutX(), minY + v.getLayoutY(), w, h);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.web("#6c72ff"));
        border.setStrokeWidth(1.5);
        border.getStrokeDashArray().addAll(6.0, 4.0);
        border.setArcWidth(4);
        border.setArcHeight(4);
        border.setMouseTransparent(true);
        selectionBorder = border;
        shapePane.getChildren().add(selectionBorder);
        addToLog("SELECT", "Selected: " + shape.getType());
    }

    private void removeSelectionBorder() {
        if (selectionBorder != null) {
            shapePane.getChildren().remove(selectionBorder);
            selectionBorder = null;
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private ShapeType getSelectedType() {
        if (selectBtn != null && selectBtn.isSelected())
            return null;
        if (rectBtn.isSelected())
            return ShapeType.RECTANGLE;
        if (circleBtn.isSelected())
            return ShapeType.CIRCLE;
        if (lineBtn.isSelected())
            return ShapeType.LINE;
        if (triangleBtn.isSelected())
            return ShapeType.TRIANGLE;
        if (ellipseBtn.isSelected())
            return ShapeType.ELLIPSE;
        if (pentagonBtn.isSelected())
            return ShapeType.PENTAGON;
        if (hexagonBtn.isSelected())
            return ShapeType.HEXAGON;
        if (diamondBtn.isSelected())
            return ShapeType.DIAMOND;
        if (starBtn.isSelected())
            return ShapeType.STAR;
        if (fillBtn != null && fillBtn.isSelected())
            return ShapeType.FILL;
        if (arrowBtn.isSelected())
            return ShapeType.ARROW;
        if (trapezoidBtn.isSelected())
            return ShapeType.TRAPEZOID;
        if (nodeBtn.isSelected())
            return ShapeType.NODE;
        if (edgeBtn.isSelected())
            return ShapeType.EDGE;
        return null;
    }

    /** Build a dashed preview shape while the user drags. */
    private Shape buildPreview(ShapeType type, double sx, double sy, double ex, double ey) {
        if (type == null)
            return null;

        // Prevent 0-size bounds by ensuring a tiny minimum size (2.0px) for preview.
        double dx = ex - sx;
        double dy = ey - sy;

        if (Math.abs(dx) < 2.0) {
            ex = (dx >= 0) ? sx + 2.0 : sx - 2.0;
        }
        if (Math.abs(dy) < 2.0) {
            ey = (dy >= 0) ? sy + 2.0 : sy - 2.0;
        }

        DrawableShape ds = shapeFactory.createShape(type, sx, sy, ex, ey, "none", "none");
        if (ds == null)
            return null;

        // Render to a dummy pane to instantiate the internal JavaFX Node
        Pane dummy = new Pane();
        ds.draw(dummy);

        Shape s = (Shape) ds.getVisualNode();
        if (s != null) {
            dummy.getChildren().remove(s); // detach from dummy pane
            s.setStroke(Color.web("#6c72ff"));
            s.setFill(Color.TRANSPARENT);
            s.getStrokeDashArray().addAll(8.0, 4.0);
            s.setMouseTransparent(true);
            s.setOpacity(0.6);
        }
        return s;
    }

    private GraphNode findNodeAt(double x, double y) {
        return graphService.getGraph().getNodes().stream()
                .filter(n -> Math.hypot(n.getCenterX() - x, n.getCenterY() - y) <= 28)
                .findFirst().orElse(null);
    }

    private GraphNode findNodeByLabel(String label) {
        return graphService.getGraph().getNodes().stream()
                .filter(n -> n.getLabel().equals(label))
                .findFirst().orElse(null);
    }

    private void clearGraphHighlights() {
        graphService.getGraph().getNodes().forEach(n -> n.highlight(false));
        graphService.getGraph().getEdges().forEach(e -> e.highlight(false));
    }

    private void resetInteractionState() {
        removeSelectionBorder();
        selectedShape = null;
        if (pendingEdgeSource != null) {
            pendingEdgeSource.highlight(false);
            pendingEdgeSource = null;
        }
        clearGraphHighlights();
    }

    private void clearCanvas() {
        shapePane.getChildren().clear();
        resetInteractionState();
    }

    private void addToLog(String action, String details) {
        logger.log(action, details);
        String entry = "[" + action + "] " + details;
        actionLogView.getItems().add(0, entry);
        if (actionLogView.getItems().size() > 200)
            actionLogView.getItems().remove(200, actionLogView.getItems().size());
    }

    private void setStatus(String msg) {
        if (statusLabel != null)
            statusLabel.setText(msg);
    }

    private void updateStatusBar() {
        if (shapeCountLabel != null) {
            int count = drawingService.getCurrentDrawing().getShapes().size();
            shapeCountLabel.setText("Shapes: " + count);
        }
        updateModeLabel();
    }

    private String toHex(Color c) {
        if (c == null || c.equals(Color.TRANSPARENT))
            return "none";
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    private String fmt(double v) {
        return String.format("%.0f", v);
    }

    // Dot grid removed for classic MS Paint retro aesthetic

    private void updateModeLabel() {
        if (modeLabel == null)
            return;
        ShapeType type = getSelectedType();
        modeLabel.setText(type == null ? "Select"
                : type.name().charAt(0)
                        + type.name().substring(1).toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private Optional<String> showChoiceDialog(String title, String header, List<String> choices) {
        return util.RetroAlert.choice(title, header, choices);
    }

    private void setupColorPalette() {
        if (colorGrid == null)
            return;

        // Warm Retro-Brown Paint colors (16 curated vintage tones)
        String[] paintColors = {
                // Top row: darker / saturated warm tones
                "#2B2B2B", "#4E3524", "#705870", "#5C7C8C",
                "#357070", "#4E6B4E", "#C85A32", "#B83A3A",
                // Bottom row: soft / lighter warm tones
                "#FAF0E6", "#E6D7C3", "#DCA498", "#AE99AE",
                "#9BB6C5", "#8FA68F", "#E6B85C", "#DC856A"
        };

        VBox topRow = new VBox(1);
        VBox botRow = new VBox(1);

        for (int i = 0; i < paintColors.length; i++) {
            Color c = Color.web(paintColors[i]);
            Button btn = new Button();
            btn.getStyleClass().add("color-square");
            btn.setStyle("-fx-background-color: " + paintColors[i] + ";");

            btn.setOnMouseClicked(e -> {
                if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    currentStrokeColor = c;
                    if (activeStrokeRect != null)
                        activeStrokeRect.setFill(c);
                } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    currentFillColor = c;
                    if (activeFillRect != null)
                        activeFillRect.setFill(c);
                }
            });

            if (i < 8) {
                topRow.getChildren().add(btn);
            } else {
                botRow.getChildren().add(btn);
            }
        }

        // Actually, old paint has 2 rows horizontally, so we need HBoxes not VBoxes.
        HBox topHBox = new HBox(1);
        HBox botHBox = new HBox(1);
        for (int i = 0; i < paintColors.length; i++) {
            Color c = Color.web(paintColors[i]);
            Button btn = new Button();
            btn.getStyleClass().add("color-square");
            btn.setStyle("-fx-background-color: " + paintColors[i] + ";");

            btn.setOnMouseClicked(e -> {
                if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    currentStrokeColor = c;
                    if (activeStrokeRect != null)
                        activeStrokeRect.setFill(c);
                } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    currentFillColor = c;
                    if (activeFillRect != null)
                        activeFillRect.setFill(c);
                }
            });

            if (i < 8)
                topHBox.getChildren().add(btn);
            else
                botHBox.getChildren().add(btn);
        }

        VBox rows = new VBox(1, topHBox, botHBox);
        colorGrid.getChildren().add(rows);

        if (activeStrokeRect != null)
            activeStrokeRect.setFill(currentStrokeColor);
        if (activeFillRect != null)
            activeFillRect.setFill(currentFillColor);
    }
}
