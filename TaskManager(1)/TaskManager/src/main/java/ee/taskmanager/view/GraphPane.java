package ee.taskmanager.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Windows Task Manageri stiilis graafik: viimased 60 mõõtmist, täidetud ala,
 * õhuke ruudustik ja ääris. Kasutab Canvas'it.
 */
public class GraphPane extends Region {
    public static final int SAMPLES = 60;

    private final Canvas canvas = new Canvas();
    private final Color color;
    private final double fixedMax;   // > 0: fikseeritud skaala (nt 100%)
    private final double minScale;   // automaatse skaala miinimum
    private final boolean grid;
    private final double[][] data;
    private double scaleMax;

    public GraphPane(Color color, double fixedMax, double minScale, int series, boolean grid) {
        this.color = color;
        this.fixedMax = fixedMax;
        this.minScale = minScale;
        this.grid = grid;
        this.data = new double[series][SAMPLES];
        this.scaleMax = fixedMax > 0 ? fixedMax : minScale;
        getChildren().add(canvas);
        setMinSize(0, 0);
    }

    public double scaleMax() {
        return scaleMax;
    }

    /** Lisab uue mõõtmise igale reale (esimene rida on täidetud, ülejäänud ainult joon). */
    public void push(double... values) {
        for (int s = 0; s < data.length; s++) {
            System.arraycopy(data[s], 1, data[s], 0, SAMPLES - 1);
            data[s][SAMPLES - 1] = s < values.length ? values[s] : 0;
        }
        if (fixedMax > 0) {
            scaleMax = fixedMax;
        } else {
            double m = 0;
            for (double[] d : data) for (double v : d) m = Math.max(m, v);
            scaleMax = Math.max(minScale, m * 1.1);
        }
        draw();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        draw();
    }

    private void draw() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        if (w <= 2 || h <= 2) return;
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);

        g.setFill(Color.rgb(30, 30, 30));
        g.fillRect(0, 0, w, h);
        g.setFill(color.deriveColor(0, 1, 1, 0.07));
        g.fillRect(0, 0, w, h);

        if (grid) {
            g.setLineWidth(1);
            g.setStroke(color.deriveColor(0, 1, 1, 0.22));
            for (int i = 1; i < 10; i++) {
                double y = Math.round(h * i / 10.0) + 0.5;
                g.strokeLine(0, y, w, y);
            }
            for (int i = 1; i < 20; i++) {
                double x = Math.round(w * i / 20.0) + 0.5;
                g.strokeLine(x, 0, x, h);
            }
        }

        double dx = w / (SAMPLES - 1);
        for (int s = 0; s < data.length; s++) {
            double[] xs = new double[SAMPLES + 2];
            double[] ys = new double[SAMPLES + 2];
            for (int i = 0; i < SAMPLES; i++) {
                xs[i] = i * dx;
                double ratio = Math.max(0, Math.min(1, data[s][i] / scaleMax));
                ys[i] = h - 1 - ratio * (h - 2);
            }
            if (s == 0) {
                xs[SAMPLES] = w;
                ys[SAMPLES] = h;
                xs[SAMPLES + 1] = 0;
                ys[SAMPLES + 1] = h;
                g.setFill(color.deriveColor(0, 1, 1, 0.28));
                g.fillPolygon(xs, ys, SAMPLES + 2);
            }
            g.setStroke(s == 0 ? color : color.deriveColor(0, 1, 1.1, 0.8));
            g.setLineWidth(grid ? 1.5 : 1.2);
            g.strokePolyline(xs, ys, SAMPLES);
        }

        g.setStroke(color);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, w - 1, h - 1);
    }
}
