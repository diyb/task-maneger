package ee.taskmanager;

/**
 * Käivitusklass IDE-s (IntelliJ) jooksutamiseks.
 * Peaklass, mis ei pärine Application-ist, väldib veateadet
 * "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
