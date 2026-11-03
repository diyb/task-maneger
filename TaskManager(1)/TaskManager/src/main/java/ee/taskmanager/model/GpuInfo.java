package ee.taskmanager.model;

/** Graafikakaardi põhiinfo. vram on baitides (0 kui teadmata). */
public record GpuInfo(String name, long vram) {
}
