package ee.taskmanager;

import ee.taskmanager.service.MonitorService;
import ee.taskmanager.view.PerformancePage;
import ee.taskmanager.view.ProcessesPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;


public class MainApp extends Application {
    private MonitorService service;

    @Override
    public void start(Stage stage) {
        ProcessesPage processes = new ProcessesPage();
        PerformancePage performance = new PerformancePage();

        Tab processesTab = new Tab("Processes", processes.root());
        Tab performanceTab = new Tab("Performance", performance.root());
        processesTab.setClosable(false);
        performanceTab.setClosable(false);
        TabPane tabs = new TabPane(processesTab, performanceTab);

        Scene scene = new Scene(tabs, 960, 680);
        scene.getStylesheets().add(getClass().getResource("/ee/taskmanager/css/style.css").toExternalForm());
        stage.setTitle("Task Manager");
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
        
        // Andmeid kogutakse taustalõimes, kasutajaliidest uuendatakse JavaFX lõimes
        service = new MonitorService(snap -> Platform.runLater(() -> {
            processes.update(snap);
            performance.update(snap);
        }));
        service.start(1);
    }

    @Override
    public void stop() {
        if (service != null) service.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
