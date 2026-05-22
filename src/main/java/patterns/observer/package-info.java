/**
 * patterns.observer – Observer Pattern documentation.
 *
 * This project implements the Observer Pattern using the JavaFX
 * {@link javafx.collections.ObservableList} built into {@code model.Drawing}.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>{@code Drawing.getShapes()} returns an {@code ObservableList<DrawableShape>}
 *       created via {@code FXCollections.observableArrayList()}.</li>
 *   <li>Any component that needs to react to shape-list changes can call
 *       {@code drawing.getShapes().addListener(ListChangeListener)} – this is the
 *       standard JavaFX Observer mechanism.</li>
 *   <li>The MainController reads the shape count via
 *       {@code drawing.getShapes().size()} after each command, keeping the status
 *       bar in sync without manual notification wiring.</li>
 * </ul>
 *
 * <p>Why no custom Observer classes exist here:</p>
 * <ul>
 *   <li>JavaFX {@code ObservableList} already provides a full, production-grade
 *       implementation of the Observer (Publish-Subscribe) pattern.</li>
 *   <li>Creating a parallel custom {@code Subject}/{@code Observer} hierarchy
 *       would duplicate functionality and reduce clarity.</li>
 *   <li>The professional choice is to leverage the platform's built-in mechanism
 *       and document it explicitly, which is done here.</li>
 * </ul>
 *
 * @see model.Drawing
 * @see javafx.collections.ObservableList
 * @see javafx.collections.ListChangeListener
 */
package patterns.observer;
