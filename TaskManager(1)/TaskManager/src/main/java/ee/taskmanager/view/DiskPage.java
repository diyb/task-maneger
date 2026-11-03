package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Disk: maht, kasutus ning lugemise/kirjutamise kiirus. */
public class DiskPage implements Page {
    private final Label usage = Ui.value(), model = new Label("–"), total = new Label("–"),
            used = new Label("–"), free = new Label("–"), read = new Label("–"), write = new Label("–");
    private final ProgressBar bar = Ui.bar();
    private final HistoryChart readChart = new HistoryChart(null, "MB/s");
    private final HistoryChart writeChart = new HistoryChart(null, "MB/s");
    private final VBox root;

    public DiskPage() {
        root = Ui.page("Disk",
                Ui.card("Space usage", usage, bar),
                Ui.card("Details",
                        Ui.kv("Disk", model),
                        Ui.kv("Total", total),
                        Ui.kv("Used", used),
                        Ui.kv("Free", free),
                        Ui.kv("Read", read),
                        Ui.kv("Write", write)),
                new HBox(16, Ui.card("Read speed", readChart), Ui.card("Write speed", writeChart)));
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        usage.setText(DataFormatter.percent(s.diskPercent()));
        bar.setProgress(s.diskPercent() / 100);
        model.setText(s.diskModel());
        total.setText(DataFormatter.bytes(s.diskTotal()));
        used.setText(DataFormatter.bytes(s.diskUsed()));
        free.setText(DataFormatter.bytes(s.diskTotal() - s.diskUsed()));
        read.setText(DataFormatter.rate(s.diskReadBps()));
        write.setText(DataFormatter.rate(s.diskWriteBps()));
        readChart.push(s.diskReadBps() / 1_048_576.0);
        writeChart.push(s.diskWriteBps() / 1_048_576.0);
    }
}
