package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Memory: kasutus protsentides, skaala lõpp näitab kogumahtu. */
public class MemoryPanel extends ResourcePanel {
    private final Label inUse = val(), available = val(), usage = val(), total = val();
    private long totalBytes;

    public MemoryPanel() {
        super("Memory", Color.web("#8b12ae"), 100, 0, 1);
        scaleFmt = v -> DataFormatter.bytes(totalBytes);
        addGraph("Memory usage");

        GridPane stats = new GridPane();
        stats.setHgap(40);
        stats.setVgap(10);
        stats.add(stat("In use", inUse), 0, 0);
        stats.add(stat("Available", available), 1, 0);

        VBox info = new VBox(4, infoRow("Total:", total), infoRow("Usage:", usage));
        HBox bottom = new HBox(50, stats, info);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        detail.getChildren().add(bottom);
    }

    @Override
    public void update(SystemSnapshot s) {
        totalBytes = s.ramTotal();
        pushGraphs(s.ramPercent());
        String pct = DataFormatter.percent(s.ramPercent());
        tileSub.setText(DataFormatter.bytes(s.ramUsed()) + " / " + DataFormatter.bytes(s.ramTotal()) + " (" + pct + ")");
        subheading.setText(DataFormatter.bytes(s.ramTotal()));
        inUse.setText(DataFormatter.bytes(s.ramUsed()));
        available.setText(DataFormatter.bytes(s.ramTotal() - s.ramUsed()));
        total.setText(DataFormatter.bytes(s.ramTotal()));
        usage.setText(pct);
    }
}
