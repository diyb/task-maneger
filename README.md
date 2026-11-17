# task-maneger
school project


# Java Task Manager

Windows 10 Task Manageri stiilis töölauarakendus (JavaFX 21 + OSHI + Maven).

## Käivitamine

Vajalik: JDK 21 ja Maven.

IDE-s (IntelliJ) käivita `ee.taskmanager.Launcher`.

## Vahekaardid

- **Processes** – tabel (Name, PID, CPU, Memory), päistes kogukasutus, lahtrid värvitakse koormuse järgi, otsing ja "End task" (küsib kinnitust)
- **Performance** – vasakul CPU / Memory / Disk / Network / GPU miniatuurgraafikutega, paremal valitud ressursi suur graafik ja arvud.
  CPU graafikul parem klõps -> "Change graph to" -> "Logical processors" näitab iga tuuma eraldi.

## Struktuur

- `MainApp` / `Launcher` – käivitus
- `service/MonitorService` – OSHI andmete kogumine taustalõimes, 1 s intervall
- `model/` – `SystemSnapshot`, `ProcessInfo`, `GpuInfo`
- `view/` – `ProcessesPage`, `PerformancePage`, `ResourcePanel` + `CpuPanel`, `MemoryPanel`, `DiskPanel`, `NetworkPanel`, `GpuPanel`, `GraphPane` (Canvas-graafik)
- `util/DataFormatter` – numbrite vormindus
- `resources/.../style.css` – Windowsi-stiilis kujundus

## Märkused

- GPU koormus ja temperatuur pole OSHI-ga üldiselt saadaval; kuvatakse mudel ja VRAM.
- CPU temperatuur kuvatakse ainult siis, kui sensorid andmeid annavad.
