package patterns.facade;

import util.RetroAlert;
import java.util.Optional;

/**
 * AlertUtil – Facade Pattern.
 *
 * Simplifies access to the RetroAlert dialog system by exposing only four
 * clean, intention-revealing methods. Callers never need to know the
 * internal complexity of RetroAlert (stage creation, styling, icons, drag
 * handlers, button wiring, etc.).
 *
 * Facade methods:
 *   showInfo  – informational message
 *   showError – error message
 *   confirm   – yes/no confirmation, returns boolean
 *   prompt    – text input dialog, returns Optional<String>
 *
 * Single Responsibility: only delegation, no UI logic here.
 */
public final class AlertUtil {
    private AlertUtil() {}

    public static void showError(String title, String msg) {
        RetroAlert.showError(title, msg);
    }

    public static void showInfo(String title, String msg) {
        RetroAlert.showInfo(title, msg);
    }

    public static boolean confirm(String title, String msg) {
        return RetroAlert.confirm(title, msg);
    }

    public static Optional<String> prompt(String title, String prompt, String defaultVal) {
        return RetroAlert.prompt(title, prompt, defaultVal);
    }
}
