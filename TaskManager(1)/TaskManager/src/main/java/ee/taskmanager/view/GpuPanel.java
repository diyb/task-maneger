package ee.taskmanager.view;

import ee.taskmanager.model.GpuInfo;
import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * GPU: OSHI ei anna üldiselt GPU koormust, seega kuvatakse ainult mudel ja VRAM.
 * Graafik jääb tühjaks, et mitte näidata väljamõeldud andmeid.
 */
public class GpuPanel extends ResourcePanel {
    private final Label note = new Label();
    private final Label vram = val();
    private boolean filled = false;

    public GpuPanel() {
        super("GPU", Color.web("#4a6fa5"), 100, 0, 1);
        tileSub.setText("Not available");
        note.setWrapText(true);
        note.getStyleClass().add("caption");
        note.setText("GPU utilization and temperature are not available on this system. "
                + "Only the model and dedicated memory are shown.");
        detail.getChildren().addAll(note, infoRow("Dedicated GPU memory:", vram));
    }

    @Override
    public void update(SystemSnapshot s) {
        if (filled) return;
        filled = true;
        List<GpuInfo> gpus = s.gpus();
        if (gpus.isEmpty()) {
            subheading.setText("No GPU detected");
            vram.setText("Not available");
            return;
        }
        GpuInfo g = gpus.get(0);
        subheading.setText(g.name());
        tileSub.setText(g.name());
        vram.setText(g.vram() > 0 ? DataFormatter.bytes(g.vram()) : "Not available");
        if (gpus.size() > 1) {
            StringBuilder sb = new StringBuilder("Other graphics adapters:");
            for (int i = 1; i < gpus.size(); i++) sb.append("\n  ").append(gpus.get(i).name());
            Label others = new Label(sb.toString());
            others.getStyleClass().add("info-key");
            detail.getChildren().add(new VBox(others));
        }
    }
}
