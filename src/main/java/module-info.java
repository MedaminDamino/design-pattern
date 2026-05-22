// module-info.java
// This module-info is intentionally minimal.
// The app is launched on the --classpath (unnamed module) so FXML reflection works
// without complex opens declarations. Compilation uses --add-modules to resolve JavaFX.
// See run.ps1 for the exact launch command.
module drawingapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Open to JavaFX for FXML controller instantiation and property binding
    opens app        to javafx.graphics, javafx.fxml;
    opens controller to javafx.fxml;
    opens model      to javafx.base, javafx.fxml;
    opens graph      to javafx.base, javafx.fxml;
    opens repository to javafx.fxml;
    opens util       to javafx.fxml;
    opens service    to javafx.fxml;

    // ── Design Pattern packages ──────────────────────────────────────────────
    opens patterns.command              to javafx.graphics, javafx.fxml;
    opens patterns.factory              to javafx.fxml;
    opens patterns.singleton            to javafx.fxml;
    opens patterns.facade               to javafx.fxml;
    opens patterns.template             to javafx.base, javafx.fxml;
    opens patterns.strategy.logging     to javafx.fxml;
    opens patterns.strategy.storage     to javafx.fxml;
    opens patterns.strategy.pathfinding to javafx.fxml;

    exports app;
    exports controller;
    exports model;
    exports graph;
    exports repository;
    exports util;
    exports service;

    // ── Design Pattern packages ──────────────────────────────────────────────
    exports patterns.command;
    exports patterns.factory;
    exports patterns.singleton;
    exports patterns.facade;
    exports patterns.template;
    exports patterns.strategy.logging;
    exports patterns.strategy.storage;
    exports patterns.strategy.pathfinding;
}
