package ee.taskmanager.view;

import ee.taskmanager.model.ProcessInfo;
import ee.taskmanager.model.SystemSnapshot;
import ee.taskmanager.util.DataFormatter;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/**
 * Processes-vahekaart Windows Task Manageri stiilis, veergude päistes kogukasutusega.
 */
public class ProcessesPage {
    private final ObservableList<ProcessInfo> master = FXCollections.observableArrayList();
    private final FilteredList<ProcessInfo> filtered = new FilteredList<>(master, p -> true);
    private final SortedList<ProcessInfo> sorted = new SortedList<>(filtered);
    private final TableView<ProcessInfo> table = new TableView<>();
    private final Label cpuTotal = new Label("0%");
    private final Label memTotal = new Label("0%");
    private final Label count = new Label();
    private final VBox root = new VBox();

    @SuppressWarnings("unchecked")
    public ProcessesPage() {
        TableColumn<ProcessInfo, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().name()));
        name.setComparator(String.CASE_INSENSITIVE_ORDER);
        name.setPrefWidth(340);

        TableColumn<ProcessInfo, Number> pid = new TableColumn<>("PID");
        pid.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().pid()));
        pid.setPrefWidth(80);
        pid.setCellFactory(col -> formattedCell(n -> String.valueOf(n.intValue())));

        TableColumn<ProcessInfo, Number> cpu = new TableColumn<>();
        cpu.setGraphic(header(cpuTotal, "CPU"));
        cpu.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().cpu()));
        cpu.setCellFactory(col -> formattedCell(n -> String.format("%.1f%%", n.doubleValue())));
        cpu.setPrefWidth(95);

        TableColumn<ProcessInfo, Number> mem = new TableColumn<>();
        mem.setGraphic(header(memTotal, "Memory"));
        mem.setCellValueFactory(c -> new ReadOnlyLongWrapper(c.getValue().memory()));
        mem.setCellFactory(col -> formattedCell(n -> DataFormatter.bytes(n.longValue())));
        mem.setPrefWidth(110);

        table.getColumns().addAll(name, pid, cpu, mem);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No processes"));
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        name.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().add(name);
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search by name or PID");
        search.setPrefWidth(220);
        search.textProperty().addListener((o, a, q) -> {
            String s = q == null ? "" : q.trim().toLowerCase();
            filtered.setPredicate(p -> s.isEmpty()
                    || p.name().toLowerCase().contains(s)
                    || String.valueOf(p.pid()).contains(s));
        });

        count.getStyleClass().add("caption");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(12, search, count, spacer);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8, 12, 8, 12));
        bottom.getStyleClass().add("bottom-bar");

        root.getChildren().addAll(table, bottom);
    }

    private static Node header(Label pct, String name) {
        pct.getStyleClass().add("col-pct");
        Label n = new Label(name);
        n.getStyleClass().add("col-name");
        VBox box = new VBox(0, pct, n);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static TableCell<ProcessInfo, Number> formattedCell(Function<Number, String> fmt) {
        return new TableCell<>() {
            {
                setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : fmt.apply(value));
            }
        };
    }

    public Node root() {
        return root;
    }

    public void update(SystemSnapshot s) {
        ProcessInfo sel = table.getSelectionModel().getSelectedItem();
        int selPid = sel == null ? -1 : sel.pid();
        master.setAll(s.processes());
        if (selPid >= 0) {
            for (ProcessInfo p : sorted) {
                if (p.pid() == selPid) {
                    table.getSelectionModel().select(p);
                    break;
                }
            }
        }
        cpuTotal.setText(DataFormatter.percent(s.cpuUsage()));
        memTotal.setText(DataFormatter.percent(s.ramPercent()));
        count.setText(s.processes().size() + " processes");
    }

}
