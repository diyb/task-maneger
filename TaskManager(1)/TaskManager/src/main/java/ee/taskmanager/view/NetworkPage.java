package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Network: download/upload kiirus ja kogumahud. */
public class NetworkPage implements Page {
    private final Label down = Ui.value(), up = Ui.value(), adapter = new Label("–"),
            received = new Label("–"), sent = new Label("–");
    private final HistoryChart downChart = new HistoryChart(null, "Mbps");
    private final HistoryChart upChart = new HistoryChart(null, "Mbps");
    private final VBox root;

    public NetworkPage() {
        root = Ui.page("Network",
                new HBox(16, Ui.card("Download", down), Ui.card("Upload", up)),
                new HBox(16, Ui.card("Download history", downChart), Ui.card("Upload history", upChart)),
                Ui.card("Details",
                        Ui.kv("Adapter", adapter),
                        Ui.kv("Received", received),
                        Ui.kv("Sent", sent)));
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        down.setText(DataFormatter.mbps(s.netDownBps()));
        up.setText(DataFormatter.mbps(s.netUpBps()));
        adapter.setText(s.netName());
        received.setText(DataFormatter.bytes(s.netReceived()));
        sent.setText(DataFormatter.bytes(s.netSent()));
        downChart.push(DataFormatter.toMbps(s.netDownBps()));
        upChart.push(DataFormatter.toMbps(s.netUpBps()));
    }
}
