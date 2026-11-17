package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Network: vastuvõtt (täidetud) ja saatmine (joon), automaatne skaala. */
public class NetworkPanel extends ResourcePanel {
    private final Label receive = val(), send = val(), adapter = val(), totalRx = val(), totalTx = val();

    public NetworkPanel() {
        super("Network", Color.web("#a35a16"), 0, 12_500, 2);
        scaleFmt = v -> DataFormatter.mbps(v);
        addGraph("Throughput");

        GridPane stats = new GridPane();
        stats.setHgap(40);
        stats.setVgap(10);
        stats.add(stat("Receive", receive), 0, 0);
        stats.add(stat("Send", send), 1, 0);

        VBox info = new VBox(4,
                infoRow("Adapter:", adapter),
                infoRow("Total received:", totalRx),
                infoRow("Total sent:", totalTx));
        HBox bottom = new HBox(50, stats, info);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        detail.getChildren().add(bottom);
    }

    @Override
    public void update(SystemSnapshot s) {
        pushGraphs(s.netDownBps(), s.netUpBps());
        tileSub.setText(String.format("S: %.1f  R: %.1f Mbps",
                DataFormatter.toMbps(s.netUpBps()), DataFormatter.toMbps(s.netDownBps())));
        subheading.setText(s.netName());
        receive.setText(DataFormatter.mbps(s.netDownBps()));
        send.setText(DataFormatter.mbps(s.netUpBps()));
        adapter.setText(s.netName());
        totalRx.setText(DataFormatter.bytes(s.netReceived()));
        totalTx.setText(DataFormatter.bytes(s.netSent()));
    }
}
