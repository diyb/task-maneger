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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** Processes: tabel, otsing, sorteerimine ja protsessi lõpetamine (kinnitusega). */
public class ProcessesPage implements Page {
    private final ObservableList<ProcessInfo> master = FXCollections.observableArrayList();
    private final FilteredList<ProcessInfo> filtered = new FilteredList<>(master, p -> true);
    private final SortedList<ProcessInfo> sorted = new SortedList<>(filtered);
    private final TableView<ProcessInfo> table = new TableView<>();
    private final Label count = new Label();
    private final VBox root;

    @SuppressWarnings("unchecked")
    public ProcessesPage() {
        TableColumn<ProcessInfo, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().name()));
        name.setPrefWidth(280);

        TableColumn<ProcessInfo, Number> pid = new TableColumn<>("PID");
        pid.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().pid()));

        TableColumn<ProcessInfo, Number> cpu = new TableColumn<>("CPU");
        cpu.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().cpu()));
        cpu.setCellFactory(col -> new TableCell<ProcessInfo, Number>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f%%", v.doubleValue()));
            }
        });

        TableColumn<ProcessInfo, Number> mem = new TableColumn<>("Memory");
        mem.setCellValueFactory(c -> new ReadOnlyLongWrapper(c.getValue().memory()));
        mem.setCellFactory(col -> new TableCell<ProcessInfo, Number>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : DataFormatter.bytes(v.longValue()));
            }
        });
        mem.setSortType(TableColumn.SortType.DESCENDING);

        table.getColumns().addAll(name, pid, cpu, mem);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.getSortOrder().add(mem);
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search by name or PID...");
        search.setPrefWidth(300);
        search.textProperty().addListener((o, a, q) -> {
            String s = q == null ? "" : q.trim().toLowerCase();
            filtered.setPredicate(p -> s.isEmpty()
                    || p.name().toLowerCase().contains(s)
                    || String.valueOf(p.pid()).contains(s));
        });

        Button kill = new Button("End process");
        kill.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        kill.setOnAction(e -> endSelected());

        count.getStyleClass().add("muted");
        HBox bar = new HBox(12, search, kill, count);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        root = Ui.page("Processes", bar, table);
        root.setPadding(new Insets(24));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private void endSelected() {
        ProcessInfo p = table.getSelectionModel().getSelectedItem();
        if (p == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "End process \"" + p.name() + "\" (PID " + p.pid() + ")?\n\n"
                        + "Ending an important system process may cause problems in Windows or loss of unsaved data.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        Optional<ProcessHandle> h = ProcessHandle.of(p.pid());
        boolean ok = h.isPresent() && h.get().destroy();
        if (!ok) {
            Alert err = new Alert(Alert.AlertType.ERROR,
                    "Could not end the process. It may have already exited or you may lack permission.");
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public boolean scrollable() {
        return false;
    }

    @Override
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
        count.setText(s.processes().size() + " processes");
    }
}
