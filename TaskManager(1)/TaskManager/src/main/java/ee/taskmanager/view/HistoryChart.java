package ee.taskmanager.view;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Joongraafik, mis hoiab viimast 60 mõõtmist. */
public class HistoryChart extends LineChart<Number, Number> {
    private static final int MAX = 60;
    private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private final Deque<Double> values = new ArrayDeque<>();

    /** @param max y-telje maksimum (nt 100) või null automaatseks; @param unit y-telje silt */
    public HistoryChart(Double max, String unit) {
        super(xAxis(), yAxis(max, unit));
        setLegendVisible(false);
        setCreateSymbols(false);
        setAnimated(false);
        setMinHeight(200);
        setPrefHeight(240);
        getData().add(series);
    }

    private static NumberAxis xAxis() {
        NumberAxis a = new NumberAxis(0, MAX - 1, 10);
        a.setTickLabelsVisible(false);
        a.setTickMarkVisible(false);
        a.setMinorTickVisible(false);
        return a;
    }

    private static NumberAxis yAxis(Double max, String unit) {
        NumberAxis a = max == null ? new NumberAxis() : new NumberAxis(0, max, max / 4);
        a.setForceZeroInRange(true);
        a.setMinorTickVisible(false);
        a.setLabel(unit);
        return a;
    }

    public void push(double v) {
        values.addLast(v);
        while (values.size() > MAX) values.removeFirst();
        List<XYChart.Data<Number, Number>> data = new ArrayList<>();
        int i = MAX - values.size();
        for (double val : values) data.add(new XYChart.Data<>(i++, val));
        series.getData().setAll(data);
    }

    public Node node() {
        return this;
    }
}
