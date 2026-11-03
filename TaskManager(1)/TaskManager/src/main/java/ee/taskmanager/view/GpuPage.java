package ee.taskmanager.view;

import ee.taskmanager.model.GpuInfo;
import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.List;

/** GPU: mudel ja VRAM. Sensorandmed (koormus, temperatuur) sõltuvad riistvarast ja draiveritest. */
public class GpuPage implements Page {
    private final VBox list = new VBox(16);
    private final VBox root;
    private boolean built = false;

    public GpuPage() {
        root = Ui.page("GPU", list,
                Ui.muted("GPU load and temperature depend on hardware and drivers and are not shown here. "
                        + "Only the model and VRAM are displayed."));
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        if (built) return;
        built = true;
        List<GpuInfo> gpus = s.gpus();
        if (gpus.isEmpty()) {
            list.getChildren().add(Ui.card("Graphics card", Ui.muted("No GPU information available on this computer.")));
            return;
        }
        for (GpuInfo g : gpus) {
            list.getChildren().add(Ui.card(g.name(),
                    Ui.muted("VRAM: " + (g.vram() > 0 ? DataFormatter.bytes(g.vram()) : "unknown"))));
        }
    }
}
