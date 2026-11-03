package ee.taskmanager.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Väikesed abimeetodid ühtse välimuse jaoks. */
public final class Ui {
    private Ui() {
    }

    public static VBox card(String title, Node... content) {
        VBox box = new VBox(8);
        box.getStyleClass().add("card");
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        box.getChildren().add(t);
        box.getChildren().addAll(content);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    public static Label value() {
        Label l = new Label("–");
        l.getStyleClass().add("card-value");
        return l;
    }

    public static ProgressBar bar() {
        ProgressBar p = new ProgressBar(0);
        p.setMaxWidth(Double.MAX_VALUE);
        return p;
    }

    public static HBox kv(String key, Label val) {
        Label k = new Label(key);
        k.getStyleClass().add("muted");
        k.setMinWidth(180);
        return new HBox(10, k, val);
    }

    public static Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        l.setWrapText(true);
        return l;
    }

    public static VBox page(String title, Node... content) {
        VBox v = new VBox(16);
        v.setPadding(new Insets(24));
        Label t = new Label(title);
        t.getStyleClass().add("page-title");
        v.getChildren().add(t);
        v.getChildren().addAll(content);
        return v;
    }
}
