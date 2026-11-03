package ee.taskmanager.service;

import ee.taskmanager.model.GpuInfo;
import ee.taskmanager.model.ProcessInfo;
import ee.taskmanager.model.SystemSnapshot;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Kogub süsteemiandmeid OSHI abil taustalõimes (ScheduledExecutorService).
 * Tulemus antakse edasi listenerile; kasutajaliidese uuendamine (Platform.runLater)
 * tehakse MainApp-is.
 */
public class MonitorService {
    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();
    private final OperatingSystem os = si.getOperatingSystem();
    private final CentralProcessor cpu = hal.getProcessor();
    private final Consumer<SystemSnapshot> listener;

    private long[] prevTicks = cpu.getSystemCpuLoadTicks();
    private long[][] prevCoreTicks = cpu.getProcessorCpuLoadTicks();
    private Map<Integer, OSProcess> prevProcs = new HashMap<>();
    private long prevTime = System.nanoTime();
    private long prevRead = -1, prevWrite = -1, prevRx = -1, prevTx = -1;

    private double lastTemp = 0;
    private int tickCount = 0;
    private List<GpuInfo> gpus;

    private ScheduledExecutorService exec;
    private ScheduledFuture<?> task;

    public MonitorService(Consumer<SystemSnapshot> listener) {
        this.listener = listener;
    }

    public synchronized void start(int seconds) {
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "monitor");
            t.setDaemon(true);
            return t;
        });
        schedule(seconds);
    }

    public synchronized void setInterval(int seconds) {
        if (exec != null) schedule(seconds);
    }

    private void schedule(int seconds) {
        if (task != null) task.cancel(false);
        task = exec.scheduleWithFixedDelay(this::tick, 0, Math.max(1, seconds), TimeUnit.SECONDS);
    }

    public synchronized void shutdown() {
        if (exec != null) exec.shutdownNow();
    }

    private void tick() {
        try {
            listener.accept(collect());
        } catch (Exception e) {
            System.err.println("Monitor error: " + e);
        }
    }

    private SystemSnapshot collect() {
        long now = System.nanoTime();
        double dt = (now - prevTime) / 1e9;
        prevTime = now;
        boolean rateOk = dt >= 0.2;

        // CPU: koormus kahe mõõtmise vahel
        double cpuUsage = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = cpu.getSystemCpuLoadTicks();
        double[] core = cpu.getProcessorCpuLoadBetweenTicks(prevCoreTicks);
        prevCoreTicks = cpu.getProcessorCpuLoadTicks();
        for (int i = 0; i < core.length; i++) core[i] *= 100;

        int logical = cpu.getLogicalProcessorCount();
        double freq = 0;
        long[] freqs = cpu.getCurrentFreq();
        int n = 0;
        for (long f : freqs) {
            if (f > 0) {
                freq += f;
                n++;
            }
        }
        freq = n > 0 ? freq / n : cpu.getProcessorIdentifier().getVendorFreq();
        freq /= 1e9;

        if (tickCount++ % 5 == 0) {
            try {
                double t = hal.getSensors().getCpuTemperature();
                lastTemp = t > 0 && !Double.isNaN(t) ? t : 0;
            } catch (Exception e) {
                lastTemp = 0;
            }
        }

        // RAM
        GlobalMemory mem = hal.getMemory();
        long ramTotal = mem.getTotal();
        long ramUsed = ramTotal - mem.getAvailable();

        // Ketas: maht
        long diskTotal = 0, diskFree = 0;
        for (OSFileStore fs : os.getFileSystem().getFileStores(true)) {
            if (fs.getTotalSpace() > 0) {
                diskTotal += fs.getTotalSpace();
                diskFree += fs.getUsableSpace();
            }
        }
        // Ketas: lugemise/kirjutamise kiirus
        List<HWDiskStore> stores = hal.getDiskStores();
        long read = 0, write = 0;
        for (HWDiskStore d : stores) {
            d.updateAttributes();
            read += d.getReadBytes();
            write += d.getWriteBytes();
        }
        double readBps = 0, writeBps = 0;
        if (prevRead >= 0 && rateOk) {
            readBps = Math.max(0, (read - prevRead) / dt);
            writeBps = Math.max(0, (write - prevWrite) / dt);
        }
        prevRead = read;
        prevWrite = write;
        String diskModel = stores.isEmpty() ? "Unknown" : stores.get(0).getModel();

        // Võrk
        long rx = 0, tx = 0, best = -1;
        String netName = "Unknown";
        for (NetworkIF nif : hal.getNetworkIFs()) {
            nif.updateAttributes();
            long r = nif.getBytesRecv(), s = nif.getBytesSent();
            rx += r;
            tx += s;
            if (r + s > best) {
                best = r + s;
                netName = nif.getDisplayName();
            }
        }
        double down = 0, up = 0;
        if (prevRx >= 0 && rateOk) {
            down = Math.max(0, (rx - prevRx) / dt);
            up = Math.max(0, (tx - prevTx) / dt);
        }
        prevRx = rx;
        prevTx = tx;

        // GPU (ainult mudel ja VRAM; sensorid sõltuvad riistvarast)
        if (gpus == null) {
            gpus = new ArrayList<>();
            try {
                for (GraphicsCard g : hal.getGraphicsCards()) gpus.add(new GpuInfo(g.getName(), g.getVRam()));
            } catch (Exception ignored) {
            }
        }

        // Protsessid
        List<OSProcess> procs = os.getProcesses(null, null, 0);
        Map<Integer, OSProcess> cur = new HashMap<>();
        List<ProcessInfo> out = new ArrayList<>(procs.size());
        for (OSProcess p : procs) {
            int pid = p.getProcessID();
            if (pid == 0) continue; // Idle
            cur.put(pid, p);
            OSProcess prev = prevProcs.get(pid);
            double load = prev == null ? 0 : 100d * p.getProcessCpuLoadBetweenTicks(prev) / logical;
            load = Math.max(0, Math.min(100, load));
            out.add(new ProcessInfo(pid, p.getName(), load, p.getResidentSetSize()));
        }
        prevProcs = cur;

        return new SystemSnapshot(
                cpu.getProcessorIdentifier().getName(), Math.max(0, Math.min(100, cpuUsage)),
                cpu.getPhysicalProcessorCount(), logical, freq, lastTemp, core,
                ramTotal, ramUsed, diskModel, diskTotal, diskTotal - diskFree,
                readBps, writeBps, netName, down, up, rx, tx, gpus, out);
    }
}
