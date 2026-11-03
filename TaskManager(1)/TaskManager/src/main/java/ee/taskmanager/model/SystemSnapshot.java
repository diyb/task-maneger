package ee.taskmanager.model;

import java.util.List;

/** Ühe mõõtmise tulemus. Kiirused on baitides sekundis, protsendid 0-100. */
public record SystemSnapshot(
        String cpuName,
        double cpuUsage,
        int physicalCores,
        int logicalCores,
        double freqGhz,
        double cpuTemp,          // 0 = andmed puuduvad
        double[] perCore,
        long ramTotal,
        long ramUsed,
        String diskModel,
        long diskTotal,
        long diskUsed,
        double diskReadBps,
        double diskWriteBps,
        String netName,
        double netDownBps,
        double netUpBps,
        long netReceived,
        long netSent,
        List<GpuInfo> gpus,
        List<ProcessInfo> processes) {

    public double ramPercent() {
        return ramTotal == 0 ? 0 : (double) ramUsed / ramTotal * 100;
    }

    public double diskPercent() {
        return diskTotal == 0 ? 0 : (double) diskUsed / diskTotal * 100;
    }
}
