package util;

import java.util.Optional;

/** AlertUtil – utility for clean Retro-styled Alert dialogs. Single Responsibility. */
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
