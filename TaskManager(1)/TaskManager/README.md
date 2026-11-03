# Java Task Manager

Windows Task Manageri sarnane töölauarakendus (JavaFX 21 + OSHI + Maven).

## Käivitamine

Vajalik: JDK 21 ja Maven.

```
mvn clean javafx:run
```

## Struktuur

- `MainApp` – käivitab JavaFX-i, külgmenüü ja lehtede vahetus
- `service/MonitorService` – OSHI andmete kogumine taustalõimes (ScheduledExecutorService)
- `model/` – `SystemSnapshot`, `ProcessInfo`, `GpuInfo`
- `view/` – lehed: Dashboard, CPU, RAM, GPU, Disk, Network, Processes, Settings
- `util/` – `DataFormatter`, `SettingsManager` (Java Preferences)
- `resources/ee/taskmanager/css/style.css` – tume ja hele teema

## Märkused

- OSHI versioon on `pom.xml`-is (`oshi.version`); kontrolli Maven Centralist uuemat.
- GPU koormus ja temperatuur pole OSHI-ga üldiselt saadaval; kuvatakse mudel ja VRAM.
- CPU temperatuur kuvatakse ainult siis, kui sensorid andmeid annavad.
- Protsessi lõpetamine küsib kinnitust.
- Kasutajaliides on koodis ehitatud (mitte FXML-is); soovi korral saab vaated hiljem Scene Builderisse viia.
