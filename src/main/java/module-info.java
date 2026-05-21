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
    opens factory    to javafx.fxml;
    opens command    to javafx.fxml;
    opens logging    to javafx.fxml;
    opens repository to javafx.fxml;
    opens util       to javafx.fxml;
    opens service    to javafx.fxml;
    opens storage    to javafx.fxml;

    exports app;
    exports controller;
    exports model;
    exports graph;
    exports factory;
    exports command;
    exports logging;
    exports repository;
    exports util;
    exports service;
    exports storage;
}
