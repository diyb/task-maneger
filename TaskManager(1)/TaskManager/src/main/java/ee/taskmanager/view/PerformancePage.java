package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Performance-vahekaart: vasakul ressursside nimekiri, paremal valitud ressursi detailid. */
public class PerformancePage {
    private final List<ResourcePanel> panels = List.of(
            new CpuPanel(), new MemoryPanel(), new DiskPanel(), new NetworkPanel(), new GpuPanel());
    private final BorderPane root = new BorderPane();
    private final StackPane right = new StackPane();

    public PerformancePage() {
        VBox left = new VBox();
        left.getStyleClass().add("perf-list");
        left.setPrefWidth(250);
        left.setMinWidth(250);
        for (ResourcePanel p : panels) {
            Node tile = p.tile();
            tile.setOnMouseClicked(e -> select(p));
            left.getChildren().add(tile);
        }
        root.setLeft(left);
        root.setCenter(right);
        select(panels.get(0));
    }

    private void select(ResourcePanel p) {
        panels.forEach(x -> x.setSelected(x == p));
        right.getChildren().setAll(p.detail());
    }

    public Node root() {
        return root;
    }

    public void update(SystemSnapshot s) {
        panels.forEach(p -> p.update(s));
    }
}
