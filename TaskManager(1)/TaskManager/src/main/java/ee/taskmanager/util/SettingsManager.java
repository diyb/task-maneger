package ee.taskmanager.util;

import java.util.prefs.Preferences;

/** Seadistuste salvestamine Java Preferences abil. */
public final class SettingsManager {
    private static final Preferences PREFS = Preferences.userNodeForPackage(SettingsManager.class);

    private SettingsManager() {
    }

    public static boolean isDark() {
        return PREFS.getBoolean("dark", true);
    }

    public static void setDark(boolean dark) {
        PREFS.putBoolean("dark", dark);
    }

    public static int intervalSeconds() {
        return PREFS.getInt("interval", 1);
    }

    public static void setIntervalSeconds(int s) {
        PREFS.putInt("interval", s);
    }

    public static String startPage() {
        return PREFS.get("startPage", "Dashboard");
    }

    public static void setStartPage(String page) {
        PREFS.put("startPage", page);
    }
}
