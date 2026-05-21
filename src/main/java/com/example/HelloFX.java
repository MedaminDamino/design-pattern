package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * HelloFX – minimal JavaFX smoke-test.
 *
 * Run via Maven:
 *   mvn javafx:run
 *
 * Or from the command line (adjust paths as needed):
 *   java --module-path "F:\C\me\GLID2 Projet\Systéme Cominiquant\javafx-sdk-25\lib" \
 *        --add-modules javafx.controls,javafx.fxml \
 *        -cp target\classes com.example.HelloFX
 */
public class HelloFX extends Application {

    private int clickCount = 0;

    @Override
    public void start(Stage primaryStage) {
        // ── Title label ──────────────────────────────────────────
        Label title = new Label("JavaFX 25 – Design Pattern Project");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#2563eb"));

        // ── Status label ─────────────────────────────────────────
        Label status = new Label("✅  JavaFX is working correctly!");
        status.setFont(Font.font("System", 14));
        status.setTextFill(Color.web("#16a34a"));

        // ── Click counter button ──────────────────────────────────
        Button btn = new Button("Click me to verify interactivity");
        btn.setStyle(
            "-fx-background-color: #2563eb;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-background-radius: 6;"
        );
        Label counter = new Label("Clicks: 0");
        counter.setFont(Font.font("System", 12));
        counter.setTextFill(Color.web("#6b7280"));

        btn.setOnAction(e -> {
            clickCount++;
            counter.setText("Clicks: " + clickCount);
        });

        // ── SDK info ──────────────────────────────────────────────
        Label sdkInfo = new Label(
            "SDK: " + System.getProperty("javafx.version", "unknown") +
            "   |   Java: " + System.getProperty("java.version")
        );
        sdkInfo.setFont(Font.font("System", 11));
        sdkInfo.setTextFill(Color.web("#9ca3af"));

        // ── Layout ───────────────────────────────────────────────
        VBox root = new VBox(18, title, status, btn, counter, sdkInfo);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 520, 320);
        primaryStage.setTitle("HelloFX – Verification");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
