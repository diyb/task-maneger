package ee.taskmanager;

import ee.taskmanager.service.MonitorService;
import ee.taskmanager.util.SettingsManager;
import ee.taskmanager.view.CpuPage;
import ee.taskmanager.view.DashboardPage;
import ee.taskmanager.view.DiskPage;
import ee.taskmanager.view.GpuPage;
import ee.taskmanager.view.NetworkPage;
import ee.taskmanager.view.Page;
import ee.taskmanager.view.ProcessesPage;
import ee.taskmanager.view.RamPage;
import ee.taskmanager.view.SettingsPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Programmi käivitaja: peaaken, külgmenüü ja lehtede vahetus. */
public class MainApp extends Application {
    private static final List<String> NAMES =
            List.of("Dashboard", "CPU", "RAM", "GPU", "Disk", "Network", "Processes", "Settings");

    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, Node> views = new LinkedHashMap<>();
    private final StackPane content = new StackPane();
    private final BorderPane root = new BorderPane();
    private MonitorService service;

    @Override
    public void start(Stage stage) {
        pages.put("Dashboard", new DashboardPage());
        pages.put("CPU", new CpuPage());
        pages.put("RAM", new RamPage());
        pages.put("GPU", new GpuPage());
        pages.put("Disk", new DiskPage());
        pages.put("Network", new NetworkPage());
        pages.put("Processes", new ProcessesPage());
        pages.put("Settings", new SettingsPage(NAMES, this::applyTheme, s -> service.setInterval(s)));

        for (var e : pages.entrySet()) {
            Page p = e.getValue();
            if (p.scrollable()) {
                ScrollPane sp = new ScrollPane(p.root());
                sp.setFitToWidth(true);
                views.put(e.getKey(), sp);
            } else {
                views.put(e.getKey(), p.root());
            }
        }

        // Külgmenüü
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(190);
        Label brand = new Label("TASK MANAGER");
        brand.getStyleClass().add("brand");
        sidebar.getChildren().add(brand);

        ToggleGroup group = new ToggleGroup();
        Map<String, ToggleButton> buttons = new LinkedHashMap<>();
        for (String name : NAMES) {
            ToggleButton b = new ToggleButton(name);
            b.setToggleGroup(group);
            b.setMaxWidth(Double.MAX_VALUE);
            b.getStyleClass().add("nav-button");
            b.setUserData(name);
            sidebar.getChildren().add(b);
            buttons.put(name, b);
        }
        group.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) {
                if (old != null) old.setSelected(true); // üks leht peab alati valitud olema
            } else {
                show((String) now.getUserData());
            }
        });

        root.setLeft(sidebar);
        root.setCenter(content);
        root.getStyleClass().add("root-pane");
        applyTheme(SettingsManager.isDark());

        String start = NAMES.contains(SettingsManager.startPage()) ? SettingsManager.startPage() : "Dashboard";
        buttons.get(start).setSelected(true);

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/ee/taskmanager/css/style.css").toExternalForm());
        stage.setTitle("Java Task Manager");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        // Andmete kogumine taustal, UI uuendus JavaFX lõimes
        service = new MonitorService(snap -> Platform.runLater(() -> {
            for (Page p : pages.values()) p.update(snap);
        }));
        service.start(SettingsManager.intervalSeconds());
    }

    private void show(String name) {
        content.getChildren().setAll(views.get(name));
    }

    private void applyTheme(boolean dark) {
        root.getStyleClass().removeAll("dark", "light");
        root.getStyleClass().add(dark ? "dark" : "light");
    }

    @Override
    public void stop() {
        if (service != null) service.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
