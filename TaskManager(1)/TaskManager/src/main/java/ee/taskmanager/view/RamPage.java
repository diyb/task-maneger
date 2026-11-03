package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/** RAM: kogu, kasutatud, vaba, protsent ja ajalugu. */
public class RamPage implements Page {
    private final Label usage = Ui.value(), total = new Label("–"), used = new Label("–"), free = new Label("–");
    private final ProgressBar bar = Ui.bar();
    private final HistoryChart chart = new HistoryChart(100.0, "%");
    private final VBox root;

    public RamPage() {
        root = Ui.page("RAM",
                Ui.card("Usage", usage, bar),
                Ui.card("Usage history", chart),
                Ui.card("Details",
                        Ui.kv("Total", total),
                        Ui.kv("Used", used),
                        Ui.kv("Free", free)));
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        usage.setText(String.format("%.1f%%", s.ramPercent()));
        bar.setProgress(s.ramPercent() / 100);
        total.setText(DataFormatter.bytes(s.ramTotal()));
        used.setText(DataFormatter.bytes(s.ramUsed()));
        free.setText(DataFormatter.bytes(s.ramTotal() - s.ramUsed()));
        chart.push(s.ramPercent());
    }
}
