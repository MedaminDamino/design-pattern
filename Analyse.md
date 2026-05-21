# JavaFX Drawing Application - Strict Project Audit & Analysis

**Date:** May 18, 2026  
**Subject:** Software Engineering, Architecture, and Validation Audit  

---

## 1. Project Overview
The project is a classic Windows 95 / MS Paint styled JavaFX vector drawing application. It supports geometric shape rendering, graph algorithms (nodes, edges, pathfinding), undo/redo via the Command pattern, saving/loading to a MySQL database using the DAO/Repository pattern, and multi-strategy logging. 

While the architecture appears fundamentally sound on paper, **actual feature correctness must be aggressively verified before submission.**

---

## 2. Professor Requirements Checklist

| Requirement | Implementation Status | Notes |
| :--- | :--- | :--- |
| **1. Shape palette** | ✅ Implemented | Toolbar exists. *Must verify all shapes render uniquely, not defaulting to rectangles.* |
| **2. Drawing area** | ✅ Implemented | Canvas pane present. *Must verify boundary constraints and scaling.* |
| **3. Undo/Redo** | ✅ Implemented | Command pattern applied. *Must verify complex graph undo/redo safety.* |
| **4. Delete** | ✅ Implemented | Select & Delete present. |
| **5. Save drawing** | ✅ Implemented | MariaDB/MySQL persistence present. *Must verify all metadata saves accurately.* |
| **6. Open drawing** | ✅ Implemented | Dialog exists. *Must verify deserialization restores the exact state and object references.* |
| **7. User action logging** | ✅ Implemented | Tracks actions via LoggerContext. |
| **8. Logging strategies** | ✅ Implemented | Console, File, Database supported via Strategy pattern. |
| **9. Graph case study** | ✅ Implemented | Nodes, edges, and Dijkstra/BFS present. *Must verify algorithm correctness and edge-node binding.* |

---

## 3. Design Pattern Analysis

1. **MVC (Model-View-Controller)**
   * **Location:** `model`, `view` (FXML/CSS), `controller` (`MainController`).
   * **Critique:** The controller handles too much canvas rendering logic. It manually adds visuals to the screen inside mouse handlers/commands instead of letting the View strictly observe the Model.
2. **Factory Pattern**
   * **Location:** `factory.ShapeFactory` and `DefaultShapeFactory`.
   * **Critique:** Implemented for UI shape creation, but **bypassed** in `ShapeRepository` which uses a hardcoded switch statement.
3. **Command Pattern**
   * **Location:** `command` package.
   * **Critique:** Generally correct, but commands currently take direct references to the JavaFX `shapePane`. Commands should mutate the model, not the UI.
4. **Strategy Pattern**
   * **Location:** `logging.LoggingStrategy`, `graph.ShortestPathStrategy`.
   * **Critique:** Cleanly implemented and functional.
5. **DAO / Repository Pattern**
   * **Location:** `repository` package.
   * **Critique:** Clean abstraction, but must be rigorously tested for SQL injection or schema mismatch issues during edge-case loading.
6. **Observer Pattern**
   * **Location:** `model.Drawing` (`ObservableList`).
   * **Critique:** Partially implemented but **ignored by the UI**. The controller does not bind listeners to automatically update the canvas, defeating the pattern's primary purpose.

---

## 4. SOLID Analysis

* **Single Responsibility Principle (SRP):** Moderate compliance. `MainController` handles excessive mouse/gesture mathematics, causing class bloat.
* **Open/Closed Principle (OCP):** Violated in `ShapeRepository.findByDrawingId()` due to a massive switch statement mapping string types to concrete classes.
* **Liskov Substitution Principle (LSP):** Good. Graph components cleanly extend `DrawableShape`.
* **Interface Segregation Principle (ISP):** Excellent. Repositories are cleanly split (`IDrawingRepository`, `IShapeRepository`).
* **Dependency Inversion Principle (DIP):** Excellent. `DrawingService` and `LoggerContext` depend on interfaces.

---

## 5. Strict Manual Validation Checklist

**Do not assume the application works until every item here is manually verified.**

### A. Drawing & Shapes
- [ ] Every shape draws its unique geometry correctly (no fallback to a generic rectangle).
- [ ] Fill tool works correctly for *every* shape (including complex polygons).
- [ ] Left-click (stroke) and Right-click (fill) correctly assign distinct colors to shapes.
- [ ] Select/Move works flawlessly for every shape without detaching selection bounding boxes.

### B. Graph & Pathfinding
- [ ] Graph nodes can be created and moved on the canvas.
- [ ] Graph edges correctly follow nodes when a connected node is dragged.
- [ ] Edge weight prompts appear, and the weight updates the model correctly.
- [ ] Dijkstra shortest path yields the mathematically correct route and highlights exactly the correct edges.
- [ ] BFS yields the correct unweighted shortest path and highlights exactly the correct edges.

### C. Undo/Redo Integrity
- [ ] Undo/Redo works perfectly after standard shape creation/deletion.
- [ ] Undo/Redo works perfectly after **Graph operations** (e.g., undoing a node deletion correctly restores its connected edges).

### D. Save / Open Integrity
- [ ] Saving a drawing and reopening it restores *exact* coordinates.
- [ ] Reopened drawings retain *exact* stroke and fill colors.
- [ ] Reopened graph nodes retain their logical IDs.
- [ ] Reopened graph edges automatically re-bind to their nodes so moving a reopened node still moves its edges.

### E. Logging & Database
- [ ] Logging correctly writes to the Console.
- [ ] Logging correctly writes to a local `.txt` file.
- [ ] Logging correctly inserts rows into the `logs` MySQL table.
- [ ] MySQL `shapes`, `graph_nodes`, and `graph_edges` tables accurately reflect the application state without orphaned rows.

---

## 6. Known Weak Points & Architectural Risks

* **Repository Factory Duplication:** `ShapeRepository` manually recreates objects. This is a risk if a new shape is added to the UI but forgotten in the database parser.
* **Controller Bloat:** The lack of a State Pattern for tools makes the `MainController` mouse event handlers fragile.
* **Observer Disconnect:** Because the `ObservableList` isn't bound to the UI pane, there is a risk of desynchronization between the database/model and what the user actually sees.
* **Memory Leaks:** `DeleteShapeCommand` must be verified to ensure deleted nodes/edges are removed from all active lists, graph adjacency matrices, and event listeners.

---

## 7. Submission Preparation

### Required Proof (Screenshots/Video)
- Screenshot of the classic MS Paint retro UI with multiple complex shapes.
- Screenshot of phpMyAdmin/MySQL showing populated `drawings`, `shapes`, and `logs` tables.
- Video snippet demonstrating the Fill tool.
- Video snippet demonstrating Graph nodes being dragged with edges following dynamically.
- Video snippet showing Dijkstra shortest path highlighting.

### Demo Video Requirements
Your project defense/demo must explicitly show:
1. Creating a complex drawing and changing logger strategies dynamically.
2. Saving the drawing to the database.
3. Clearing the canvas and completely reopening the saved drawing.
4. Dragging a graph node to prove edge binding survived the database reload.
5. Executing Dijkstra vs BFS to prove the Strategy pattern works.
6. Hitting Undo 5 times to prove the Command pattern works.

---

## 8. Final Evaluation

* **Final Readiness:** **READY ONLY AFTER PASSING THE STRICT VALIDATION CHECKLIST.**
* **Overall Quality:** The architecture proves a strong grasp of design patterns, but the current UI-to-Model coupling requires rigorous manual QA to guarantee stability during grading.
