package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Drawing – MVC Model.
 * Holds metadata and the live list of shapes.
 * Observer Pattern: ObservableList notifies listeners on changes.
 */
public class Drawing {
    private int id;
    private String name;
    private String createdAt;
    private String filePath;
    private final ObservableList<DrawableShape> shapes = FXCollections.observableArrayList();

    public Drawing() {}
    public Drawing(String name) { this.name = name; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public ObservableList<DrawableShape> getShapes() { return shapes; }

    public void addShape(DrawableShape s) { shapes.add(s); }
    public void removeShape(DrawableShape s) { shapes.remove(s); }

    @Override
    public String toString() { return name + " (id=" + id + ")"; }
}
