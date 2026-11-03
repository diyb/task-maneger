package ee.taskmanager.util;

/** Numbrite ilus kuvamine. */
public final class DataFormatter {
    private DataFormatter() {
    }

    public static String bytes(long b) {
        if (b < 0) b = 0;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double v = b;
        int i = 0;
        while (v >= 1024 && i < units.length - 1) {
            v /= 1024;
            i++;
        }
        return i == 0 ? b + " B" : String.format("%.1f %s", v, units[i]);
    }

    public static String percent(double v) {
        return String.format("%.0f%%", v);
    }

    /** Baiti sekundis -> loetav kiirus (MB/s jne). */
    public static String rate(double bytesPerSec) {
        return bytes((long) bytesPerSec) + "/s";
    }

    /** Baiti sekundis -> megabitti sekundis (baidid * 8). */
    public static double toMbps(double bytesPerSec) {
        return bytesPerSec * 8 / 1_000_000.0;
    }

    public static String mbps(double bytesPerSec) {
        return String.format("%.1f Mbps", toMbps(bytesPerSec));
    }
}
