package ee.taskmanager.model;

/** Üks töötav protsess. cpu on protsent (0-100), memory baitides. */
public record ProcessInfo(int pid, String name, double cpu, long memory) {
}
