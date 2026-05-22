package util;

import model.Drawing;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Optional;

/**
 * OpenDrawingDialog – Redesigned as an authentic Windows 95 style dialog.
 */
public final class OpenDrawingDialog {

    // ── Warm Retro-Brown palette constants (mirrors CSS design tokens) ──────
    private static final String C_BG    = "#eae2d5"; /* win-bg: vintage cream sand */
    private static final String C_LIGHT = "#f5eedf"; /* win-light: creamy bevel highlight */
    private static final String C_WHITE = "#fdfcf7"; /* win-white: warm off-white */
    private static final String C_DARK  = "#9c8978"; /* win-dark: muted taupe shadow */
    private static final String C_BLACK = "#2b201a"; /* win-black: soft espresso text */
    private static final String C_BLUE  = "#5c4636"; /* win-blue: espresso title bar/selection */

    private OpenDrawingDialog() {}

    public static Optional<Drawing> show(List<Drawing> drawings) {
        Dialog<Drawing> dialog = new Dialog<>();
        dialog.setTitle("Open");
        dialog.setHeaderText(null);
        try {
            dialog.getDialogPane().getStylesheets().add(OpenDrawingDialog.class.getResource("/style/app.css").toExternalForm());
        } catch (Exception ignored) {}

        ButtonType openBtn   = new ButtonType("Open",   ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(openBtn, cancelBtn);

        // ── Dialog pane base styling (Warm Retro-Brown) ──────────────────
        dialog.getDialogPane().setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_LIGHT + " " + C_BLACK + " " + C_BLACK + " " + C_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-font-family: 'MS Sans Serif', 'Tahoma';" +
            "-fx-font-size: 11px;"
        );

        // ── Outer container ───────────────────────────────────────────────
        VBox root = new VBox(6);
        root.setPadding(new Insets(8));
        root.setPrefWidth(380);


        // ── Project list ──────────────────────────────────────────────────
        ListView<Drawing> list = new ListView<>(FXCollections.observableArrayList(drawings));
        list.setPrefHeight(200);
        list.setStyle(
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_DARK + " " + C_LIGHT + " " + C_LIGHT + " " + C_DARK + ";" +
            "-fx-border-width: 2;" +
            "-fx-padding: 2;"
        );

        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Drawing item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: " + C_WHITE + ";");
                    return;
                }
                
                String dateStr = item.getCreatedAt() != null ? item.getCreatedAt() : "";
                setText("📄 " + item.getName() + "  (" + dateStr + ")");
                setStyle("-fx-text-fill: " + C_BLACK + "; -fx-font-family: 'MS Sans Serif'; -fx-font-size: 11px;");
                
                if (isSelected()) {
                    setStyle("-fx-background-color: " + C_BLUE + "; -fx-text-fill: " + C_LIGHT + "; -fx-font-family: 'MS Sans Serif'; -fx-font-size: 11px;");
                }
            }
        });

        // ── File name input row ───────────────────────────────────────────
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label fileNameLbl = new Label("File name:");
        fileNameLbl.setPrefWidth(60);
        fileNameLbl.setStyle("-fx-text-fill: " + C_BLACK + ";");
        
        TextField fileNameField = new TextField();
        fileNameField.setEditable(false);
        fileNameField.setPrefWidth(220);
        fileNameField.setStyle(
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_DARK + " " + C_LIGHT + " " + C_LIGHT + " " + C_DARK + ";" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";"
        );
        nameRow.getChildren().addAll(fileNameLbl, fileNameField);

        HBox typeRow = new HBox(6);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        Label fileTypeLbl = new Label("Files of type:");
        fileTypeLbl.setPrefWidth(60);
        fileTypeLbl.setStyle("-fx-text-fill: " + C_BLACK + ";");
        
        ComboBox<String> fileTypeCombo = new ComboBox<>();
        fileTypeCombo.getItems().add("Paint Files (*.drw)");
        fileTypeCombo.getSelectionModel().selectFirst();
        fileTypeCombo.setPrefWidth(220);
        fileTypeCombo.setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_DARK + " " + C_LIGHT + " " + C_LIGHT + " " + C_DARK + ";" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + C_BLACK + ";"
        );
        typeRow.getChildren().addAll(fileTypeLbl, fileTypeCombo);

        // ── Enable open only on selection ─────────────────────────────────
        Button openButton = (Button) dialog.getDialogPane().lookupButton(openBtn);
        openButton.setDisable(true);
        
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean hasSel = (sel != null);
            openButton.setDisable(!hasSel);
            if (hasSel) fileNameField.setText(sel.getName());
        });
        
        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                openButton.fire();
            }
        });

        root.getChildren().addAll(list, nameRow, typeRow);
        dialog.getDialogPane().setContent(root);

        // ── Button bar styling (Win95 outsets via CSS) ───────────────────
        Button openB   = (Button) dialog.getDialogPane().lookupButton(openBtn);
        Button cancelB = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        
        if (openB != null) {
            openB.getStyleClass().add("btn-win95");
        }
        if (cancelB != null) {
            cancelB.getStyleClass().add("btn-win95");
        }

        dialog.setResultConverter(btn -> btn == openBtn ? list.getSelectionModel().getSelectedItem() : null);

        return dialog.showAndWait();
    }
}
