package util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Optional;

/**
 * RetroAlert – A beautifully authentic Windows 95/98 style alert/dialog utility.
 * Using a completely custom modal stage to achieve pixel-perfect retro aesthetic
 * with beveled borders, Tahoma/MS Sans Serif fonts, solid blue title bars, and custom vector icons.
 */
public final class RetroAlert {

    public enum RetroIcon {
        INFO, WARNING, ERROR, QUESTION
    }

    // ── Warm Retro-Brown palette constants (mirrors CSS design tokens) ──────
    private static final String C_BG     = "#eae2d5"; /* win-bg: vintage cream sand */
    private static final String C_LIGHT  = "#f5eedf"; /* win-light: creamy bevel highlight */
    private static final String C_WHITE  = "#fdfcf7"; /* win-white: warm off-white */
    private static final String C_DARK   = "#9c8978"; /* win-dark: muted taupe shadow */
    private static final String C_BLACK  = "#2b201a"; /* win-black: soft espresso text */
    private static final String C_BLUE   = "#5c4636"; /* win-blue: espresso title bar */

    private static boolean confirmResult = false;
    private static String promptResult = null;

    private RetroAlert() {}

    public static void showInfo(String title, String message) {
        show(title, message, RetroIcon.INFO, List.of(
            new ButtonChoice("OK", () -> {})
        ));
    }

    public static void showWarning(String title, String message) {
        show(title, message, RetroIcon.WARNING, List.of(
            new ButtonChoice("OK", () -> {})
        ));
    }

    public static void showError(String title, String message) {
        show(title, message, RetroIcon.ERROR, List.of(
            new ButtonChoice("OK", () -> {})
        ));
    }

    public static boolean confirm(String title, String message) {
        confirmResult = false;
        show(title, message, RetroIcon.QUESTION, List.of(
            new ButtonChoice("Yes", () -> confirmResult = true),
            new ButtonChoice("No",  () -> confirmResult = false)
        ));
        return confirmResult;
    }

    public static Optional<String> prompt(String title, String message, String defaultValue) {
        promptResult = null;
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        VBox frame = new VBox(0);
        frame.setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + "," +
                               C_BG + " " + C_DARK + " " + C_DARK + " " + C_BG + ";" +
            "-fx-border-width: 1, 1;" +
            "-fx-border-insets: 0, 1;"
        );

        HBox titleBar = createTitleBar(title, stage);

        HBox body = new HBox(12);
        body.setPadding(new Insets(16, 12, 12, 12));
        body.setAlignment(Pos.TOP_LEFT);

        Node iconNode = createIcon(RetroIcon.QUESTION);

        VBox textAndControls = new VBox(8);
        textAndControls.setAlignment(Pos.TOP_LEFT);

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(300);
        msgLbl.setFont(Font.font("MS Sans Serif", 11));
        msgLbl.setTextFill(Color.BLACK);

        TextField inputField = new TextField(defaultValue);
        inputField.setPrefWidth(250);
        inputField.setStyle(
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_DARK + " " + C_LIGHT + " " + C_LIGHT + " " + C_DARK + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";" +
            "-fx-padding: 3;" +
            "-fx-font-family: 'MS Sans Serif', 'Tahoma';" +
            "-fx-font-size: 11px;"
        );

        textAndControls.getChildren().addAll(msgLbl, inputField);
        body.getChildren().addAll(iconNode, textAndControls);

        HBox buttons = new HBox(6);
        buttons.setPadding(new Insets(0, 12, 12, 12));
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        Button okBtn = createWin95Button("OK", () -> {
            promptResult = inputField.getText();
            stage.close();
        });
        Button cancelBtn = createWin95Button("Cancel", () -> {
            promptResult = null;
            stage.close();
        });

        buttons.getChildren().addAll(okBtn, cancelBtn);
        frame.getChildren().addAll(titleBar, body, buttons);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.showAndWait();

        return Optional.ofNullable(promptResult);
    }

    public static Optional<String> choice(String title, String message, List<String> choices) {
        promptResult = null;
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        VBox frame = new VBox(0);
        frame.setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + "," +
                               C_BG + " " + C_DARK + " " + C_DARK + " " + C_BG + ";" +
            "-fx-border-width: 1, 1;" +
            "-fx-border-insets: 0, 1;"
        );

        HBox titleBar = createTitleBar(title, stage);

        HBox body = new HBox(12);
        body.setPadding(new Insets(16, 12, 12, 12));
        body.setAlignment(Pos.TOP_LEFT);

        Node iconNode = createIcon(RetroIcon.QUESTION);

        VBox textAndControls = new VBox(8);
        textAndControls.setAlignment(Pos.TOP_LEFT);

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(300);
        msgLbl.setFont(Font.font("MS Sans Serif", 11));
        msgLbl.setTextFill(Color.BLACK);

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(choices);
        if (!choices.isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
        combo.setPrefWidth(250);
        combo.setStyle(
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_DARK + " " + C_LIGHT + " " + C_LIGHT + " " + C_DARK + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";" +
            "-fx-font-family: 'MS Sans Serif', 'Tahoma';" +
            "-fx-font-size: 11px;"
        );

        textAndControls.getChildren().addAll(msgLbl, combo);
        body.getChildren().addAll(iconNode, textAndControls);

        HBox buttons = new HBox(6);
        buttons.setPadding(new Insets(0, 12, 12, 12));
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        Button okBtn = createWin95Button("OK", () -> {
            promptResult = combo.getValue();
            stage.close();
        });
        Button cancelBtn = createWin95Button("Cancel", () -> {
            promptResult = null;
            stage.close();
        });

        buttons.getChildren().addAll(okBtn, cancelBtn);
        frame.getChildren().addAll(titleBar, body, buttons);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.showAndWait();

        return Optional.ofNullable(promptResult);
    }

    private static void show(String title, String message, RetroIcon icon, List<ButtonChoice> actions) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        VBox frame = new VBox(0);
        frame.setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + "," +
                               C_BG + " " + C_DARK + " " + C_DARK + " " + C_BG + ";" +
            "-fx-border-width: 1, 1;" +
            "-fx-border-insets: 0, 1;"
        );

        HBox titleBar = createTitleBar(title, stage);

        HBox body = new HBox(12);
        body.setPadding(new Insets(16, 12, 12, 12));
        body.setAlignment(Pos.TOP_LEFT);

        Node iconNode = createIcon(icon);

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(300);
        msgLbl.setFont(Font.font("MS Sans Serif", 11));
        msgLbl.setTextFill(Color.BLACK);

        body.getChildren().addAll(iconNode, msgLbl);

        HBox buttons = new HBox(6);
        buttons.setPadding(new Insets(0, 12, 12, 12));
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        for (ButtonChoice act : actions) {
            Button btn = createWin95Button(act.text, () -> {
                act.action.run();
                stage.close();
            });
            buttons.getChildren().add(btn);
        }

        frame.getChildren().addAll(titleBar, body, buttons);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static HBox createTitleBar(String title, Stage stage) {
        HBox bar = new HBox();
        bar.setStyle("-fx-background-color: " + C_BLUE + "; -fx-padding: 3 4 3 6;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 11));
        titleLbl.setTextFill(Color.web(C_LIGHT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        String closeBtnStyle =
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + ";" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";" +
            "-fx-font-family: 'MS Sans Serif', 'Arial';" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 0 4 0 4;" +
            "-fx-min-width: 16px;" +
            "-fx-min-height: 14px;" +
            "-fx-max-width: 16px;" +
            "-fx-max-height: 14px;";
        closeBtn.setStyle(closeBtnStyle);
        closeBtn.setOnMousePressed(e -> closeBtn.setStyle(closeBtnStyle + "-fx-border-color: " + C_BLACK + " " + C_LIGHT + " " + C_LIGHT + " " + C_BLACK + "; -fx-padding: 1 3 -1 5;"));
        closeBtn.setOnMouseReleased(e -> closeBtn.setStyle(closeBtnStyle));
        closeBtn.setOnAction(e -> stage.close());

        bar.getChildren().addAll(titleLbl, spacer, closeBtn);

        // Drag handlers to make the undecorated stage fully draggable
        double[] dragDelta = new double[2];
        bar.setOnMousePressed(me -> {
            dragDelta[0] = stage.getX() - me.getScreenX();
            dragDelta[1] = stage.getY() - me.getScreenY();
        });
        bar.setOnMouseDragged(me -> {
            stage.setX(me.getScreenX() + dragDelta[0]);
            stage.setY(me.getScreenY() + dragDelta[1]);
        });

        return bar;
    }

    private static Button createWin95Button(String text, Runnable action) {
        Button btn = new Button(text);

        String win95BtnStyle =
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + ","
                               + C_BG + " " + C_DARK + " " + C_DARK + " " + C_BG + ";" +
            "-fx-border-width: 1, 1;" +
            "-fx-border-insets: 0, 1;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";" +
            "-fx-font-family: 'MS Sans Serif', 'Tahoma';" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 3 18 3 18;" +
            "-fx-min-width: 75px;";

        btn.setStyle(win95BtnStyle);

        btn.setOnMousePressed(e -> btn.setStyle(win95BtnStyle +
            "-fx-border-color: " + C_BLACK + " " + C_LIGHT + " " + C_LIGHT + " " + C_BLACK + ","
                               + C_DARK + " " + C_BG + " " + C_BG + " " + C_DARK + ";" +
            "-fx-padding: 4 17 2 19;"));
        btn.setOnMouseReleased(e -> btn.setStyle(win95BtnStyle));
        btn.setOnAction(e -> action.run());

        return btn;
    }

    private static Node createIcon(RetroIcon type) {
        StackPane root = new StackPane();
        root.setPrefSize(32, 32);
        root.setMaxSize(32, 32);

        switch (type) {
            case INFO -> {
                Circle c = new Circle(14, Color.web(C_BLUE));
                c.setStroke(Color.web(C_BLACK));
                c.setStrokeWidth(1);
                Text t = new Text("i");
                t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 18));
                t.setFill(Color.web(C_LIGHT));
                root.getChildren().addAll(c, t);
            }
            case WARNING -> {
                Polygon poly = new Polygon(
                    14, 2,
                    2, 28,
                    26, 28
                );
                poly.setFill(Color.web("#e6b85c")); /* mustard yellow */
                poly.setStroke(Color.web(C_BLACK));
                poly.setStrokeWidth(1.5);
                Text t = new Text("!");
                t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 18));
                t.setFill(Color.web(C_BLACK));
                StackPane.setMargin(t, new Insets(4, 0, 0, 0));
                root.getChildren().addAll(poly, t);
            }
            case ERROR -> {
                Circle c = new Circle(14, Color.web("#b83a3a")); /* warm red */
                c.setStroke(Color.web(C_BLACK));
                c.setStrokeWidth(1);
                Text t = new Text("X");
                t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 14));
                t.setFill(Color.web(C_LIGHT));
                root.getChildren().addAll(c, t);
            }
            case QUESTION -> {
                Circle c = new Circle(14, Color.web("#357070")); /* cozy teal */
                c.setStroke(Color.web(C_BLACK));
                c.setStrokeWidth(1);
                Text t = new Text("?");
                t.setFont(Font.font("MS Sans Serif", FontWeight.BOLD, 18));
                t.setFill(Color.web(C_LIGHT));
                root.getChildren().addAll(c, t);
            }
        }
        return root;
    }

    private static class ButtonChoice {
        final String text;
        final Runnable action;

        ButtonChoice(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }
}
