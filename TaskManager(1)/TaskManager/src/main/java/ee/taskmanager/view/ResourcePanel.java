package ee.taskmanager.view;

import ee.taskmanager.model.SystemSnapshot;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Function;

/**
 * Üks ressurss Performance-vahekaardil: vasakul tile (miniatuurgraafik + tekst)
 * ja paremal detailvaade (suur graafik + arvud).
 */
public abstract class ResourcePanel {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    protected final GraphPane mini;
    protected final GraphPane big;
    protected final VBox detail = new VBox(6);
    protected final StackPane graphHolder = new StackPane();
    protected final Label heading = new Label();
    protected final Label subheading = new Label();
    protected final Label tileTitle = new Label();
    protected final Label tileSub = new Label();
    protected Function<Double, String> scaleFmt = v -> String.format("%.0f%%", v);

    private final HBox tile = new HBox(10);
    private final Label scaleLabel = new Label();

    protected ResourcePanel(String title, Color color, double fixedMax, double minScale, int series) {
        mini = new GraphPane(color, fixedMax, minScale, series, false);
        mini.setMinSize(72, 54);
        mini.setPrefSize(72, 54);
        mini.setMaxSize(72, 54);
        big = new GraphPane(color, fixedMax, minScale, series, true);

        tileTitle.setText(title);
        tileTitle.getStyleClass().add("tile-title");
        tileSub.getStyleClass().add("tile-sub");
        tile.getStyleClass().add("perf-tile");
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.getChildren().addAll(mini, new VBox(2, tileTitle, tileSub));

        heading.setText(title);
        heading.getStyleClass().add("detail-title");
        subheading.getStyleClass().add("detail-model");
        detail.setPadding(new Insets(16, 20, 16, 20));
        detail.getChildren().add(new HBox(10, heading, spacer(), subheading));
    }

    public Node tile() {
        return tile;
    }

    public Node detail() {
        return detail;
    }

    public void setSelected(boolean selected) {
        tile.pseudoClassStateChanged(SELECTED, selected);
    }

    public abstract void update(SystemSnapshot s);

    /** Lisab detailvaatesse suure graafiku telgede siltidega. */
    protected void addGraph(String yLabel) {
        Label y = new Label(yLabel);
        y.getStyleClass().add("caption");
        scaleLabel.getStyleClass().add("caption");
        scaleLabel.setText(scaleFmt.apply(big.scaleMax()));

        graphHolder.getChildren().add(big);
        graphHolder.setMinHeight(160);
        graphHolder.setPrefHeight(320);
        VBox.setVgrow(graphHolder, Priority.ALWAYS);

        Label left = new Label("60 seconds");
        Label right = new Label("0");
        left.getStyleClass().add("caption");
        right.getStyleClass().add("caption");

        detail.getChildren().addAll(
                new HBox(y, spacer(), scaleLabel),
                graphHolder,
                new HBox(left, spacer(), right));
    }

    protected void pushGraphs(double... values) {
        mini.push(values);
        big.push(values);
        scaleLabel.setText(scaleFmt.apply(big.scaleMax()));
    }

    protected static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    protected static Label val() {
        return new Label("–");
    }

    /** Suur arv koos halli pealkirjaga. */
    protected static VBox stat(String caption, Label value) {
        Label c = new Label(caption);
        c.getStyleClass().add("caption");
        value.getStyleClass().add("stat-value");
        return new VBox(0, c, value);
    }

    /** Väike rida "Nimi: väärtus". */
    protected static HBox infoRow(String key, Label value) {
        Label k = new Label(key);
        k.getStyleClass().add("info-key");
        k.setMinWidth(150);
        return new HBox(8, k, value);
    }
}
