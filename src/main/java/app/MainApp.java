package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp – JavaFX Application entry point.
 * MVC: launches the View (FXML) which wires the Controller automatically.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(
                getClass().getResource("/style/app.css").toExternalForm());

        primaryStage.setTitle("JavaFX Drawing App – Design Patterns");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
