# Technical Report: JavaFX Drawing & Graph Application with Design Patterns

## 1. Introduction

### Project Goal
The primary objective of this project is the design, implementation, and analysis of an interactive desktop drawing and graph modeling application. Rather than serving simply as a minimal paint utility, the application was engineered to act as a case study in modern software architecture, demonstrating how Gang of Four (GoF) design patterns and SOLID principles can be integrated into a client-side JavaFX application.

### Motivation and Scope
In GUI application development, changes in user interface components often lead to unintended side effects in business logic or data storage layers. This project was initiated to explore structural frameworks that isolate the graphical user interface (GUI) from state validation, pathfinding algorithms, logging pipelines, and persistence engines. By structuring the application into discrete, interchangeable layers, the system remains highly resilient to changes, such as swapping persistence databases, appending new drawing shapes, or modifying action log targets.

### Main Functionalities
*   **Vector Shape Engine**: Provides interactive creation, movement, selection, and color-filling of 11 distinct shapes. These include standard geometries (Rectangle, Circle, Line, Ellipse) and complex polygon structures (Triangle, Pentagon, Hexagon, Diamond, Star, Arrow, Trapezoid).
*   **Logical Graph Modeling**: Enables real-time topology drawing on the canvas. Vertices (Graph Nodes) and weighted connections (Graph Edges) can be dynamically linked, updated, and moved. Edges anchor automatically and translate dynamically as nodes are dragged.
*   **Interactive Pathfinding**: Solves shortest-path queries on the drawn graph using BFS (Breadth-First Search) or Dijkstra's algorithm, visually highlighting the result.
*   **Undo/Redo System**: Manages canvas history with unlimited undo and redo capabilities for all modifications (creating, deleting, moving, and coloring elements).
*   **Storage Strategy Decoupling**: Dynamically switches save/open workflows between a relational database (SQL) and custom serialized files (`.drw`) without restarting the session.
*   **Structured Audit Logging**: Offers hot-swappable log outputs, letting developers route action records to the Console, a local text file, or database tables.

### Tech Stack
*   **Core Platform**: Java 23 & JavaFX 25
*   **Build & Lifecycle Management**: Maven
*   **Database & Driver**: MySQL & JDBC
*   **Styling Engine**: CSS (Cascading Style Sheets)

---

## 2. Project Architecture

The application implements a clean **Model-View-Controller (MVC)** architectural pattern, separating the presentational layout, application state, and execution dispatching. This structure is further organized into service and repository layers to maintain strict boundaries.

```
+-------------------------------------------------------------+
|                        VIEW LAYER                           |
|  - main-view.fxml (Layout)      - app.css (Retro Stylesheet) |
|  - RetroAlert (Custom Modals)  - OpenDrawingDialog          |
+------------------------------+------------------------------+
                               | Event Dispatching
                               v
+-------------------------------------------------------------+
|                     CONTROLLER LAYER                        |
|  - MainController (UI Events, Canvas Listeners, State Sync) |
+-----------------------+--------------+----------------------+
                        |              |
     Logger Delegation  |              | Service Calls
                        v              v
+-------------------------------+ +---------------------------+
|         LOGGING LAYER         | |       SERVICE LAYER       |
| - LoggerContext (Singleton)   | | - DrawingService          |
| - LoggingStrategy (Interface) | | - GraphService            |
+---------------+---------------+ +------------+--------------+
                |                              |
                | Writes Logs                  | Mutates State & Persists
                v                              v
+-------------------------------+ +---------------------------+
|        DATABASE LAYER         | |    STORAGE / PERSISTENCE  |
| - LogRepository               | | - StorageContext (Strategy)|
| - DatabaseManager (Singleton) | | - File / DB Strategies    |
| - MySQL Database Connections  | | - Drawing/Shape Repos     |
+-------------------------------+ +---------------------------+
                                               |
                                               v
                                  +---------------------------+
                                  |    DOMAIN MODEL LAYER     |
                                  | - Drawing                 |
                                  | - DrawableShape (Interface)|
                                  | - AbstractShape / Polygon |
                                  +---------------------------+
```

### MVC Separation of Responsibilities

#### View
Defined declaratively in `main-view.fxml` and customized via `app.css`. The view describes structural controls, buttons, dropdowns, list views, and the central drawing pane. The view contains no programming logic; it fires events defined in the controller. Custom Windows 95 retro alert dialogs (`RetroAlert` and `OpenDrawingDialog`) also populate the View layer, ensuring the styling is kept consistent.

#### Model
Includes domain classes like `Drawing`, `DrawableShape`, `AbstractShape`, `PolygonShape`, `GraphNode`, and `GraphEdge`. The `Drawing` class holds metadata and an `ObservableList<DrawableShape>` representing the shape list. The models capture drawing coordinates, stroke widths, fill states, and structural weights, containing zero references to JavaFX FXML elements.

#### Controller
`MainController` acts as the interface mediator. It registers event listeners on canvas mouse events, tracks tool buttons, coordinates selection boxes, and handles menu triggers. It handles the canvas interactions, translating user inputs (drags, clicks, key presses) into service calls.

### Tiered Architecture Layering

1.  **Controller Layer**: Handles GUI event handlers and visual feedback (such as dashed selection boxes).
2.  **Service Layer**: Encapsulates business validation. `DrawingService` runs drawing transactions, coordinates undo stacks, and manages database triggers. `GraphService` manages node connectivity, distance validations, and pathfinding queries.
3.  **Repository (DAO) Layer**: Insulates the application from JDBC execution. `DrawingRepository`, `ShapeRepository`, and `LogRepository` write and parse database records, returning clean domain models.
4.  **Storage Layer**: Abstracts persistence using strategy wrappers (`FileDrawingStorageStrategy`, `DatabaseDrawingStorageStrategy`), managed via a single `StorageContext`.

### Scalability and Maintainability Analysis
By using this layered structure, each module remains isolated. For example, replacing MySQL with PostgreSQL only requires rewriting the SQL queries in the Repository classes, leaving the canvas rendering and drawing service untouched. Adding a new shape type requires zero changes to the storage controller or database schema, demonstrating high system extensibility.

---

## 3. Main Functionalities

### Shape Drawing
Allows users to choose a shape tool and drag-and-drop on the canvas to draw shapes.
*   **Functionality**: Triggers a live dashed preview outline during mouse drag, committing the finalized vector shape when the mouse is released.
*   **Involved Classes**: `MainController`, `ShapeFactory`, `DefaultShapeFactory`, `DrawableShape`, `AbstractShape`, `PolygonShape`.
*   **Interactions**:
    1.  `MainController.onCanvasMousePressed` stores initial coordinates (`pressX`, `pressY`).
    2.  `onCanvasMouseDragged` calls `buildPreview(...)` to render a dashed boundary line on the screen.
    3.  `onCanvasMouseReleased` invokes `shapeFactory.createShape(...)` to generate the concrete shape.
    4.  The controller creates an `AddShapeCommand` and passes it to the `DrawingService` to draw it and register the transaction.

### Graph Node Creation
Enables drawing topological graphs by placing nodes on the canvas.
*   **Functionality**: Places numbered circular badges on the canvas, checking that they do not overlap existing nodes.
*   **Involved Classes**: `MainController`, `GraphNode`, `GraphService`, `AddGraphNodeCommand`.
*   **Interactions**:
    1.  The user clicks on the canvas with the `NODE` tool active.
    2.  `MainController` passes the coordinates to `GraphService.addNode(...)`.
    3.  The service validates that the coordinates are clear of other nodes.
    4.  An `AddGraphNodeCommand` is executed to add the node to both the `Drawing` model and the visual pane.

### Graph Edge Creation
Renders weighted connections between nodes to build the graph topology.
*   **Functionality**: Selects a start and end node, prompts for a weight value, and draws a connection line with a text label showing the weight.
*   **Involved Classes**: `MainController`, `GraphEdge`, `GraphService`, `AddGraphEdgeCommand`, `RetroAlert`.
*   **Interactions**:
    1.  The user clicks node A, which is saved in `pendingEdgeSource`.
    2.  The user clicks node B.
    3.  `RetroAlert.prompt(...)` opens a modal asking for the edge weight.
    4.  If approved, `AddGraphEdgeCommand` is executed. The edge links the two nodes and renders a connector line.

### Pathfinding
Visualizes pathfinding algorithms on the drawn graph topology.
*   **Functionality**: Resolves pathfinding queries using a chosen algorithm (Dijkstra or BFS) and highlights the resulting path.
*   **Involved Classes**: `MainController`, `GraphService`, `ShortestPathStrategy`, `BFSStrategy`, `DijkstraStrategy`, `GraphNode`, `GraphEdge`.
*   **Interactions**:
    1.  The user clicks **Pathfinding -> Find Path** in the UI.
    2.  Prompt boxes ask for the start and target Node IDs.
    3.  `GraphService` executes the query using the strategy selected in `algorithmCombo`.
    4.  The resulting nodes and edges are highlighted on the canvas in deep retro navy blue.

### Undo/Redo
Maintains session state and lets users rollback changes.
*   **Functionality**: Supports undo and redo operations for all canvas edits.
*   **Involved Classes**: `Command`, `CommandManager`, `DrawingService`, and concrete commands (e.g. `AddShapeCommand`, `DeleteShapeCommand`, `MoveShapeCommand`).
*   **Interactions**:
    1.  `MainController.onUndo` calls `DrawingService.undo()`.
    2.  `DrawingService` delegates to `CommandManager.undo()`.
    3.  The manager pops the last command from the history stack, calls `undo()`, and pushes the command to the redo stack.
    4.  The shape is added back or removed from the drawing and canvas.

### Save/Open Drawing
Saves and loads drawings.
*   **Functionality**: Saves the active drawing using the current persistence strategy.
*   **Involved Classes**: `MainController`, `StorageContext`, `DrawingStorageStrategy`.
*   **Interactions**:
    1.  The user clicks **Save**.
    2.  `MainController` passes the active drawing and pane to `StorageContext.save(...)`.
    3.  The context delegates saving to the selected strategy (File or Database).

### File Storage
Saves drawings locally as custom flat-files.
*   **Functionality**: Serializes drawing configurations to `.drw` files.
*   **Involved Classes**: `FileDrawingStorageStrategy`, `DrawingFileSerializer`, `LoadedDrawing`.
*   **Interactions**:
    1.  If the drawing has not been saved before, a `FileChooser` prompts the user for a path. Subsequent saves overwrite the file silently.
    2.  `DrawingFileSerializer` parses and writes lines of shape metadata.
    3.  The parser handles locale differences, accepting both comma-separated and dot-separated coordinates.

### Database Storage
Saves drawings to relational database tables.
*   **Functionality**: Saves drawings and shapes to a central SQL server.
*   **Involved Classes**: `DatabaseDrawingStorageStrategy`, `DrawingRepository`, `ShapeRepository`, `DatabaseManager`.
*   **Interactions**:
    1.  The strategy saves the drawing name and timestamp, returning a generated database ID.
    2.  The strategy saves all drawing shapes linked to that drawing ID.

### Logging System
Logs application events at runtime.
*   **Functionality**: Records edits and events using the active logger output.
*   **Involved Classes**: `LoggerContext`, `LoggingStrategy`, `ConsoleLoggingStrategy`, `FileLoggingStrategy`, `DatabaseLoggingStrategy`.
*   **Interactions**:
    1.  When an action runs, the controller calls `LoggerContext.getInstance().log(action, details)`.
    2.  The context forwards the request to the active strategy (Console, File, or Database).

### Fill and Stroke Colors
Sets styling and background fill for shapes.
*   **Functionality**: Selects colors from the palette to fill shapes or set their outline colors.
*   **Involved Classes**: `MainController`, `FillShapeCommand`, `AbstractShape`.
*   **Interactions**:
    1.  The user selects colors in the palette, updating `currentStrokeColor` and `currentFillColor`.
    2.  Clicking a shape with the `FILL` tool executes `FillShapeCommand`.
    3.  The shape's color hex is updated, and it is redrawn on the canvas.

### Selection and Moving Shapes
Allows selecting and moving shapes on the canvas.
*   **Functionality**: Selects shapes and drags them to move them.
*   **Involved Classes**: `MainController`, `MoveShapeCommand`, `DrawableShape`.
*   **Interactions**:
    1.  Selecting a shape renders a dashed red selection border around it.
    2.  Dragging the shape translates it on the canvas.
    3.  On release, `MoveShapeCommand` registers the move in the undo history stack.

---

## 4. Design Patterns Used

### Factory Method Pattern
The application uses the Factory Method pattern to instantiate drawing shapes without coupling the controller to concrete shape constructors.

```
       +-----------------------------------+
       |          <<interface>>            |
       |           ShapeFactory            |
       +-----------------------------------+
       | + createShape(type, ...)          |
       +-----------------+-----------------+
                         ^
                         | Implements
       +-----------------+-----------------+
       |        DefaultShapeFactory        |
       +-----------------+-----------------+
                         |
                         | Instantiates
                         v
       +-----------------+-----------------+
       |          <<interface>>            |
       |          DrawableShape            |
       +-----------------+-----------------+
       | + draw(Pane)                      |
       +-------+-------------------+-------+
               ^                   ^
               | Extends           | Extends
       +-------+-------+   +-------+-------+
       |  CircleShape  |   | RectangleShape|
       +---------------+   +---------------+
```

*   **Purpose**: Decouples the user interface from concrete class instantiations.
*   **Why Chosen**: Prevents modifying controller code when adding new shapes.
*   **Classes Involved**: `ShapeFactory` (interface), `DefaultShapeFactory` (implementation), `DrawableShape` (abstraction), `ShapeType` (enum).
*   **Benefits**: Centralizes creation logic. Adding a new shape (e.g. `OctagonShape`) only requires implementing `DrawableShape` and adding its case in the factory, keeping the controller untouched.

### Strategy Pattern
The Strategy pattern is used in three systems: persistence, logging, and pathfinding.

```
                  +--------------------------------+
                  |         StorageContext         |
                  +--------------------------------+
                  | - strategy: StorageStrategy    |
                  +---------------+----------------+
                                  |
                                  | Delegates
                                  v
                  +--------------------------------+
                  |         <<interface>>          |
                  |     DrawingStorageStrategy     |
                  +---------------+----------------+
                                  ^
                                  |
         +------------------------+------------------------+
         |                                                 |
+--------+-----------------------+                +--------+-----------------------+
| DatabaseDrawingStorageStrategy |                |  FileDrawingStorageStrategy   |
+--------------------------------+                +--------------------------------+
```

*   **Purpose**: Defines a family of algorithms and systems, making them interchangeable at runtime.
*   **Why Chosen**: Allows switching persistence targets, logging outputs, and pathfinding algorithms on the fly based on UI options.
*   **Classes Involved**:
    *   *Persistence*: `DrawingStorageStrategy`, `DatabaseDrawingStorageStrategy`, `FileDrawingStorageStrategy`, `StorageContext`.
    *   *Logging*: `LoggingStrategy`, `ConsoleLoggingStrategy`, `FileLoggingStrategy`, `DatabaseLoggingStrategy`, `LoggerContext`.
    *   *Pathfinding*: `ShortestPathStrategy`, `BFSStrategy`, `DijkstraStrategy`.
*   **Benefits**: Eliminates complex conditional checks (`if/else`) inside controller logic. New strategies can be added as new classes without modifying existing ones.

### Command Pattern
The Command pattern handles the application's transaction history and undo/redo stacks.

```
       +-----------------------+
       |    CommandManager     |
       +-----------------------+
       | - undoStack: Deque    |
       | - redoStack: Deque    |
       +-----------+-----------+
                   | Invokes
                   v
       +-----------------------+
       |     <<interface>>     |
       |        Command        |
       +-----------+-----------+
       | + execute()           |
       | + undo()              |
       +-----------+-----------+
                   ^
                   | Implements
       +-----------+-----------+
       |    AddShapeCommand    |
       +-----------------------+
```

*   **Purpose**: Encapsulates a request as an object, allowing operations to be queued, logged, and undone.
*   **Why Chosen**: Enables undo and redo actions by capturing the state of each canvas edit.
*   **Classes Involved**: `Command` (interface), `CommandManager` (invoker), and concrete commands (`AddShapeCommand`, `DeleteShapeCommand`, `MoveShapeCommand`, `FillShapeCommand`, `AddGraphNodeCommand`, `AddGraphEdgeCommand`, `UpdateEdgeWeightCommand`).
*   **Benefits**: Decouples the object invoking the action from the object executing it. Command execution is tracked in undo/redo history stacks managed by `CommandManager`.

### DAO/Repository Pattern
This pattern isolates database access and persistence operations.
*   **Purpose**: Provides an interface for CRUD operations, separating database queries from business services.
*   **Why Chosen**: Keeps JDBC code and SQL queries out of controllers and services.
*   **Classes Involved**: `IDrawingRepository`, `DrawingRepository`, `IShapeRepository`, `ShapeRepository`, `ILogRepository`, `LogRepository`, `DatabaseManager`.
*   **Benefits**: Swapping database configurations (such as moving from SQLite to MySQL) only requires updates in the repository implementations, keeping the rest of the application unchanged.

### Model-View-Controller (MVC)
MVC separates the application's visual interface, domain data, and control logic.
*   **Purpose**: Separates UI presentation, domain data models, and event controllers to reduce coupling.
*   **Why Chosen**: Standard architectural pattern for JavaFX applications that helps organize layout and logic files.
*   **Classes Involved**: `main-view.fxml` & `app.css` (View), `MainController` (Controller), `Drawing` & `DrawableShape` (Model).
*   **Benefits**: Separating the UI layout from the model data makes the code cleaner, easier to maintain, and simpler to test.

### Singleton Pattern
The Singleton pattern provides single global access points for shared resources.
*   **Purpose**: Ensures a class has only one instance and provides a global access point to it.
*   **Why Chosen**: Coordinates access to shared resources like database connection pools and logging contexts.
*   **Classes Involved**: `DatabaseManager`, `LoggerContext`.
*   **Benefits**: Prevents opening multiple database connections or duplicating logger context files.

---

## 5. SOLID Principles

The architecture follows the SOLID principles to ensure code quality and maintainability.

### Single Responsibility Principle (SRP)
Each class in the system has a single, well-defined responsibility.
*   *Application*: `DrawingFileSerializer` only handles parsing and serializing `.drw` files. It has no knowledge of how those files are selected (FileChooser) or how the canvas renders them. Similarly, `DrawingRepository` only handles drawings SQL operations, keeping business logic clean.

### Open/Closed Principle (OCP)
Classes are open for extension but closed for modification.
*   *Application*: Adding a new shape (e.g., `OctagonShape`) only requires implementing `PolygonShape` and adding a case in `DefaultShapeFactory`. `MainController` and `DrawingService` remain completely untouched, operating purely on `DrawableShape`.

### Liskov Substitution Principle (LSP)
Subclasses can replace their parent classes without changing application behavior.
*   *Application*: All shapes, including standard geometry (e.g., `CircleShape`) and custom graph structures (`GraphNode`, `GraphEdge`), inherit from `AbstractShape` (which implements `DrawableShape`). The canvas rendering loops and command objects manipulate them uniformly through the `DrawableShape` interface.

### Interface Segregation Principle (ISP)
Clients are not forced to depend on methods they do not use.
*   *Application*: Interfaces are kept cohesive and minimal. `Command` declares only `execute()` and `undo()`. `ShortestPathStrategy` declares only `findPath()`. Clients are not forced to implement methods they don't need.

### Dependency Inversion Principle (DIP)
High-level modules do not depend on low-level modules; both depend on abstractions.
*   *Application*: The `MainController` does not directly instantiate `FileDrawingStorageStrategy`. Instead, it interacts with `StorageContext` which depends on the `DrawingStorageStrategy` abstraction. Dependencies are injected via setters, allowing dynamic swapping.

---

## 6. User Interface and UX Design

The user interface uses a retro desktop style, combining classic Win95 aesthetics with modern UI controls.

| Visual Interface Component | Design Choice and Rationale |
| :--- | :--- |
| **Retro Theme** | Inspired by Windows 95/98 styling with flat gray backgrounds, double-beveled borders, and clean layouts. |
| **Grid Palette** | Offers quick access to standard colors with previews for active stroke and fill settings. |
| **Toolbar** | Positioned on the left side, containing clear tool buttons for shape and graph modeling. |
| **Dual Panels** | The right-side panel organizes configuration settings, including persistence selection and logging strategies. |
| **Live Log Viewer** | A dedicated list box displays real-time action logs, providing immediate feedback for user edits. |
| **Status Bar** | Located at the bottom of the window, displaying coordinate trackers, shape counts, and the current active tool. |

```
+------------------------------------------------------------------------+
|  🎨 JavaFX Retro Paint - [Untitled]                                _ [] X |
+------------------------------------------------------------------------+
|  File  Edit  Pathfinding  Help                                         |
+--------------------------+------------------------------+--------------+
|  Tools                   |  Drawing Canvas              |  Controls    |
|  [Select]                |                              |  Storage:    |
|  [Rectangle]             |      (Circle Shape)          |  [File     v]|
|  [Circle]                |      startX: 295.2           |              |
|  [Line]                  |      startY: 193.6           |  Logging:    |
|  [Node]                  |                              |  [Console  v]|
|  [Edge]                  |                              |              |
|                          |                              |  Action Log: |
|  Colors                  |                              |  - Add Shape |
|  [ ] [ ] [ ]             |                              |  - Move Shape|
|  Stroke: [Red]           |                              |  - Save File |
|  Fill:   [Blue]          |                              |              |
+--------------------------+------------------------------+--------------+
|  Active Tool: Circle | Canvas Coordinates: (412, 280) | Total Shapes: 3|
+------------------------------------------------------------------------+
```

### UX Usability Choices
*   **Immediate Feedback**: Drag actions display a dashed preview, showing the shape's final bounds before it is committed.
*   **Interactive Node Positioning**: Vertices drag smoothly, and connected edges update their anchor coordinates automatically.
*   **Aesthetic Styling**: The retro-inspired double bevels, classic scroll bars, and clear font choices make the application intuitive and visually cohesive.

---

## 7. Database and Persistence

The persistence layer uses a strategy system to swap save targets on the fly.

### Database Persistence Model
*   **MySQL & JDBC Connection**: `DatabaseManager` runs double-checked locking to build a synchronized singleton connection.
*   **Automatic Schema Generation**: If tables are missing on startup, the application creates them automatically.

```sql
CREATE TABLE IF NOT EXISTS drawings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shapes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    start_x DOUBLE, start_y DOUBLE,
    end_x DOUBLE, end_y DOUBLE,
    width DOUBLE, height DOUBLE, radius DOUBLE,
    stroke_color VARCHAR(10), fill_color VARCHAR(10),
    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS graph_nodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    node_id INT NOT NULL,
    label VARCHAR(50),
    x DOUBLE, y DOUBLE,
    stroke_color VARCHAR(10), fill_color VARCHAR(10),
    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS graph_edges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    source_node_id INT NOT NULL,
    target_node_id INT NOT NULL,
    weight DOUBLE,
    stroke_color VARCHAR(10),
    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### File-based Serialization Format
Files are stored in a human-readable text format (`.drw`). The parser supports standard locale coordinate formats, handling both dot and comma decimals (e.g. `295.2` and `295,2`).

```text
DRAWING_NAME=Network_Topology
SHAPE=CIRCLE|295.200000|193.600000|395.200000|293.600000|#2b2b2b|#faf0e6
SHAPE=PENTAGON|355.200000|106.400000|455.200000|206.400000|#2b2b2b|#faf0e6
SHAPE=NODE|500.000000|300.000000|1|Vertex 1|#2b2b2b|#faf0e6
SHAPE=NODE|650.000000|300.000000|2|Vertex 2|#2b2b2b|#faf0e6
SHAPE=EDGE|500.000000|300.000000|650.000000|300.000000|1|2|15.5|#2b2b2b
```

---

## 8. Challenges and Improvements

### Technical Challenges

#### Decoupling Logging and Persistence Strategies
Initially, persistence actions were hardcoded to logging strategies. This dependency was resolved by separating `DrawingStorageStrategy` (persistence actions) from `LoggingStrategy` (event audits). The storage strategies handle files and databases independently, and write logs using the decoupled logging facade.

#### Reversing Node Deletion with Connected Edges
In graph modeling, deleting a node also deletes its connected edges. To support undo, `DeleteShapeCommand` must cache all removed edges. When undoing the deletion, the command restores the node and reconnects all edges in their exact prior positions.

#### Cross-Platform Decimal Formats
Coordinate parsing on different operating systems can crash due to local decimal notations (dot vs comma). This was resolved by implementing decimal normalization in `DrawingFileSerializer`, replacing comma delimiters with standard dots before parsing.

### Future Improvements
1.  **Multi-user Collaboration**: Adding web sockets to support shared online canvas editing.
2.  **Vector Export Options**: Exporting drawing canvases to SVG, PDF, or PNG formats.
3.  **Real-Time Path Costing**: Dynamic recalculation of edge weights based on coordinates as nodes are dragged.
4.  **Canvas Zoom and Pan**: Support for navigating large graphs using the mouse scroll wheel.

---

## 9. Conclusion

The JavaFX Drawing Application project demonstrates a clean implementation of SOLID principles and GoF design patterns in desktop software. Organizing the application into MVC components, services, and repositories makes the codebase easy to maintain and extend.

Key patterns like the Factory Method, Strategy, Command, and DAO/Repository isolate the UI layout from the database and file persistence logic. These decoupling patterns prevent changes in the user interface from affecting data integrity, resulting in a maintainable project structure ready for future extensions.
