package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/** CPU: kogukoormus, soovi korral iga loogilise protsessori graafik (parem klõps graafikul). */
public class CpuPanel extends ResourcePanel {
    private static final Color COLOR = Color.web("#117dbb");

    private final Label util = val(), speed = val(), procs = val(), threads = val(), uptime = val();
    private final Label base = val(), sockets = val(), cores = val(), logical = val(), temp = val();
    private final GridPane coreGrid = new GridPane();
    private final List<GraphPane> coreGraphs = new ArrayList<>();

    public CpuPanel() {
        super("CPU", COLOR, 100, 0, 1);
        addGraph("% Utilization");

        coreGrid.setHgap(2);
        coreGrid.setVgap(2);
        coreGrid.setVisible(false);
        graphHolder.getChildren().add(coreGrid);

        RadioMenuItem overall = new RadioMenuItem("Overall utilization");
        RadioMenuItem perCore = new RadioMenuItem("Logical processors");
        ToggleGroup tg = new ToggleGroup();
        overall.setToggleGroup(tg);
        perCore.setToggleGroup(tg);
        overall.setSelected(true);
        tg.selectedToggleProperty().addListener((o, a, b) -> {
            boolean cores = b == perCore;
            big.setVisible(!cores);
            coreGrid.setVisible(cores);
        });
        ContextMenu menu = new ContextMenu(new Menu("Change graph to", null, overall, perCore));
        graphHolder.setOnContextMenuRequested(e -> menu.show(graphHolder, e.getScreenX(), e.getScreenY()));

        GridPane stats = new GridPane();
        stats.setHgap(40);
        stats.setVgap(10);
        stats.add(stat("Utilization", util), 0, 0);
        stats.add(stat("Speed", speed), 1, 0);
        stats.add(stat("Processes", procs), 0, 1);
        stats.add(stat("Threads", threads), 1, 1);
        stats.add(stat("Up time", uptime), 0, 2);

        VBox info = new VBox(4,
                infoRow("Base speed:", base),
                infoRow("Sockets:", sockets),
                infoRow("Cores:", cores),
                infoRow("Logical processors:", logical),
                infoRow("Temperature:", temp));
        HBox bottom = new HBox(50, stats, info);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        detail.getChildren().add(bottom);
    }

    @Override
    public void update(SystemSnapshot s) {
        pushGraphs(s.cpuUsage());
        String pct = DataFormatter.percent(s.cpuUsage());
        String ghz = String.format("%.2f GHz", s.freqGhz());
        tileSub.setText(pct + "  " + ghz);
        subheading.setText(s.cpuName());
        util.setText(pct);
        speed.setText(ghz);
        procs.setText(String.valueOf(s.processCount()));
        threads.setText(String.valueOf(s.threadCount()));
        uptime.setText(DataFormatter.uptime(s.uptimeSec()));
        base.setText(s.baseGhz() > 0 ? String.format("%.2f GHz", s.baseGhz()) : "Not available");
        sockets.setText(String.valueOf(s.sockets()));
        cores.setText(String.valueOf(s.physicalCores()));
        logical.setText(String.valueOf(s.logicalCores()));
        temp.setText(s.cpuTemp() > 0 ? String.format("%.0f °C", s.cpuTemp()) : "Not available");

        double[] pc = s.perCore();
        if (coreGraphs.size() != pc.length) buildCores(pc.length);
        for (int i = 0; i < pc.length; i++) coreGraphs.get(i).push(pc[i]);
    }

    private void buildCores(int n) {
        coreGrid.getChildren().clear();
        coreGrid.getColumnConstraints().clear();
        coreGrid.getRowConstraints().clear();
        coreGraphs.clear();
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil(n / (double) cols);
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            coreGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / rows);
            coreGrid.getRowConstraints().add(rc);
        }
        for (int i = 0; i < n; i++) {
            GraphPane g = new GraphPane(COLOR, 100, 0, 1, false);
            coreGraphs.add(g);
            coreGrid.add(g, i % cols, i / cols);
        }
    }
}
