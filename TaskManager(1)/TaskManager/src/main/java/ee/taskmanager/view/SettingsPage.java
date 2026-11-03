package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.SettingsManager;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Settings: teema, uuendamise intervall ja käivitusleht. Salvestub Preferences kaudu. */
public class SettingsPage implements Page {
    private final VBox root;

    public SettingsPage(List<String> pageNames, Consumer<Boolean> onTheme, IntConsumer onInterval) {
        ComboBox<String> theme = new ComboBox<>();
        theme.getItems().addAll("Dark", "Light");
        theme.setValue(SettingsManager.isDark() ? "Dark" : "Light");
        theme.setOnAction(e -> {
            boolean dark = "Dark".equals(theme.getValue());
            SettingsManager.setDark(dark);
            onTheme.accept(dark);
        });

        ComboBox<Integer> interval = new ComboBox<>();
        interval.getItems().addAll(1, 2, 3, 5);
        interval.setValue(SettingsManager.intervalSeconds());
        interval.setOnAction(e -> {
            if (interval.getValue() == null) return;
            SettingsManager.setIntervalSeconds(interval.getValue());
            onInterval.accept(interval.getValue());
        });

        ComboBox<String> start = new ComboBox<>();
        start.getItems().addAll(pageNames);
        start.setValue(SettingsManager.startPage());
        start.setOnAction(e -> {
            if (start.getValue() != null) SettingsManager.setStartPage(start.getValue());
        });

        root = Ui.page("Settings",
                Ui.card("Appearance", Ui.muted("Theme"), theme),
                Ui.card("Data refresh", Ui.muted("Update interval (seconds)"), interval),
                Ui.card("Startup", Ui.muted("Page opened when the program starts"), start));
        root.setMaxWidth(500);
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void update(SystemSnapshot s) {
        // Settings-leht ei vaja andmeid
    }
}
