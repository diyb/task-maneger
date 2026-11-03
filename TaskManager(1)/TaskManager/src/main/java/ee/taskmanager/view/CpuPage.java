package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** CPU: mudel, kasutus, tuumad, sagedus, temperatuur ja iga tuuma koormus. */
public class CpuPage implements Page {
    private final Label model = new Label("–"), usage = Ui.value(), cores = new Label("–"),
            freq = new Label("–"), temp = new Label("–");
    private final ProgressBar usageBar = Ui.bar();
    private final HistoryChart chart = new HistoryChart(100.0, "%");
    private final FlowPane coreBox = new FlowPane(12, 12);
    private final List<ProgressBar> coreBars = new ArrayList<>();
    private final List<Label> coreLabels = new ArrayList<>();
    private final VBox root;

    public CpuPage() {
        model.getStyleClass().add("muted");
        root = Ui.page("CPU",
                model,
                Ui.card("Usage", usage, usageBar),
                Ui.card("Usage history", chart),
                Ui.card("Details",
                        Ui.kv("Physical / logical cores", cores),
                        Ui.kv("Frequency", freq),
                        Ui.kv("Temperature", temp)),
                Ui.card("Per-core usage", coreBox));
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        model.setText(s.cpuName());
        usage.setText(DataFormatter.percent(s.cpuUsage()));
        usageBar.setProgress(s.cpuUsage() / 100);
        cores.setText(s.physicalCores() + " / " + s.logicalCores());
        freq.setText(String.format("%.2f GHz", s.freqGhz()));
        temp.setText(s.cpuTemp() > 0 ? String.format("%.0f °C", s.cpuTemp()) : "Not available");
        chart.push(s.cpuUsage());

        double[] pc = s.perCore();
        if (coreBars.size() != pc.length) {
            coreBox.getChildren().clear();
            coreBars.clear();
            coreLabels.clear();
            for (int i = 0; i < pc.length; i++) {
                Label l = new Label();
                ProgressBar b = new ProgressBar(0);
                b.setPrefWidth(110);
                coreBars.add(b);
                coreLabels.add(l);
                coreBox.getChildren().add(new VBox(4, l, b));
            }
        }
        for (int i = 0; i < pc.length; i++) {
            coreBars.get(i).setProgress(pc[i] / 100);
            coreLabels.get(i).setText("Core " + i + ": " + DataFormatter.percent(pc[i]));
        }
    }
}
