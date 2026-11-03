package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Dashboard: kõige tähtsam info ühel ekraanil. */
public class DashboardPage implements Page {
    private final Label cpu = Ui.value(), ram = Ui.value(), gpu = Ui.value(), disk = Ui.value();
    private final ProgressBar cpuBar = Ui.bar(), ramBar = Ui.bar(), diskBar = Ui.bar();
    private final Label gpuName = Ui.muted("–"), diskSub = Ui.muted("–"), ramSub = Ui.muted("–");
    private final HistoryChart cpuChart = new HistoryChart(100.0, "%");
    private final HistoryChart ramChart = new HistoryChart(100.0, "%");
    private final VBox root;

    public DashboardPage() {
        HBox cards = new HBox(16,
                Ui.card("CPU", cpu, cpuBar),
                Ui.card("RAM", ram, ramBar, ramSub),
                Ui.card("GPU", gpu, gpuName),
                Ui.card("DISK", disk, diskBar, diskSub));
        HBox charts = new HBox(16,
                Ui.card("CPU usage history", cpuChart),
                Ui.card("RAM usage history", ramChart));
        root = Ui.page("System overview", cards, charts);
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        cpu.setText(DataFormatter.percent(s.cpuUsage()));
        cpuBar.setProgress(s.cpuUsage() / 100);
        ram.setText(DataFormatter.percent(s.ramPercent()));
        ramBar.setProgress(s.ramPercent() / 100);
        ramSub.setText(DataFormatter.bytes(s.ramUsed()) + " / " + DataFormatter.bytes(s.ramTotal()));
        disk.setText(DataFormatter.percent(s.diskPercent()));
        diskBar.setProgress(s.diskPercent() / 100);
        diskSub.setText(DataFormatter.bytes(s.diskUsed()) + " / " + DataFormatter.bytes(s.diskTotal()));
        gpu.setText("N/A");
        gpuName.setText(s.gpus().isEmpty() ? "No GPU info available" : s.gpus().get(0).name());
        cpuChart.push(s.cpuUsage());
        ramChart.push(s.ramPercent());
    }
}
