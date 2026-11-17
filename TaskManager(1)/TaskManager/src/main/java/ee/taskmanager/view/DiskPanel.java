package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Disk: lugemise (täidetud) ja kirjutamise (joon) kiirus. */
public class DiskPanel extends ResourcePanel {
    private final Label read = val(), write = val(), capacity = val(), used = val(), free = val();

    public DiskPanel() {
        super("Disk", Color.web("#4da63a"), 0, 1_048_576, 2);
        scaleFmt = v -> DataFormatter.rate(v);
        addGraph("Disk transfer rate");

        GridPane stats = new GridPane();
        stats.setHgap(40);
        stats.setVgap(10);
        stats.add(stat("Read speed", read), 0, 0);
        stats.add(stat("Write speed", write), 1, 0);

        VBox info = new VBox(4,
                infoRow("Capacity:", capacity),
                infoRow("Used:", used),
                infoRow("Free:", free));
        HBox bottom = new HBox(50, stats, info);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        detail.getChildren().add(bottom);
    }

    @Override
    public void update(SystemSnapshot s) {
        pushGraphs(s.diskReadBps(), s.diskWriteBps());
        tileSub.setText(DataFormatter.percent(s.diskPercent()) + " used");
        subheading.setText(s.diskModel());
        read.setText(DataFormatter.rate(s.diskReadBps()));
        write.setText(DataFormatter.rate(s.diskWriteBps()));
        capacity.setText(DataFormatter.bytes(s.diskTotal()));
        used.setText(DataFormatter.bytes(s.diskUsed()));
        free.setText(DataFormatter.bytes(s.diskTotal() - s.diskUsed()));
    }
}
