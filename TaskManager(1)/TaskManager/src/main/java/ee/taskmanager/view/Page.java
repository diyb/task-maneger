package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import javafx.scene.Node;

/** Üks programmi leht (vaade). */
public interface Page {
    Node root();

    void update(SystemSnapshot s);

    /** Kas leht pakitakse kerimisalasse (Processes-leht ei pakita). */
    default boolean scrollable() {
        return true;
    }
}
