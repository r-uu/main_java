# Projektkontext: jeeeraaah (Java / Jakarta EE Backend & JavaFX Frontend)

## Hinweise für KI-Agenten

### Token sparen – Kontextstrategie

- **Immer zuerst diese Datei lesen**, bevor Fragen zum Projekt gestellt werden.
- Diese Datei **nach relevanten Änderungen aktualisieren** (inkl. Datum).
- Bei Fragen zu JPMS: `JPMS-REFERENCE.md` lesen, nicht neu erklären lassen.
- Bei Fragen zu Build-Problemen: `BUILD-TROUBLESHOOTING.md` zuerst prüfen.
- Keine allgemeinen Java/Jakarta EE/JPMS-Erklärungen liefern, die bereits in den Doku-Dateien stehen.
- Bei Fragen zum Kotlin-CMP-Frontend: [context.md](file:///mnt/c/Users/r-uu/develop/github/kotlin/cmp/main/context.md) heranziehen.
- Änderungen an Backend-API-Schnittstellen immer in beiden Projekten prüfen.

### Coding-Konventionen (immer einhalten)

- Alle neuen Module **müssen** `module-info.java` haben (JPMS Pflicht).
- Dependency-Versionen **nur** in `bom/pom.xml` angeben, niemals in Modul-POMs.
- `opens`-Direktiven immer qualifiziert (mit Modulnamen), nie offen.
- Implementierungsklassen in `internal`-Pakete, die **nicht exportiert** werden.
- `provided` scope nur wenn die Abhängigkeit **nicht** in `module-info.java` steht.

### Antwortformat

- Code-Vorschläge immer vollständig (keine `// ... rest bleibt gleich`-Platzhalter).
- Bei `module-info.java`-Änderungen immer die vollständige Datei zeigen.
- Bei POM-Änderungen immer prüfen, ob die Version ins BOM gehört.

---

## Metadaten

| Feld                      | Wert                                                     |
|---------------------------|----------------------------------------------------------|
| **Projektname**           | jeeeraaah                                                |
| **Typ**                   | Enterprise-Aufgabenverwaltung (Backend & JavaFX Desktop) |
| **Sprache**               | Java (JDK 25 GraalVM)                                    |
| **Frameworks**            | Jakarta EE 10, MicroProfile 6.1, JavaFX 25               |
| **Build-System**          | Maven 3.9+                                               |
| **Infrastruktur**         | Docker (PostgreSQL, Keycloak, JasperReports)             |
| **Laufzeitumgebung**      | Open Liberty 25.0.0.12 (WSL Ubuntu)                      |
| **Lokaler Pfad (WSL)**    | `/home/r-uu/develop/github/java/main`                    |
| **Repo**                  | https://github.com/r-uu/main_java                        |
| **Letzte Aktualisierung** | 2026-06-08                                               |

---

## Projektzweck

JEEERAAAH ist eine Jakarta EE 10 Enterprise-Aufgabenverwaltung und dient als Referenzimplementierung für modulares Java mit JPMS. Basis für zwei Publikationen:

- **["JPMS in Action – jeeeraaah"](root/app/jeeeraaah/doc/md/jpms%20in%20action%20-%20jeeeraaah/jpms%20in%20action%20-%20jeeeraaah.md)**
- **["Modular Software in Java"](root/app/jeeeraaah/doc/md/modular%20software%20in%20java/modular%20software%20in%20java.md)**

---

## Tech Stack

| Komponente  | Technologie   | Version   |
|-------------|---------------|-----------|
| JDK         | GraalVM       | 25        |
| App Server  | OpenLiberty   | 25.0.0.12 |
| Frontend    | JavaFX        | 25        |
| Datenbank   | PostgreSQL    | 16        |
| IAM         | Keycloak      | Docker    |
| Reports     | JasperReports | Docker    |
| Logging     | Log4j2        | –         |
| DI (FX)     | Weld SE (CDI) | –         |
| REST Client | Jersey Client | –         |

---

## Maven-Modulstruktur

```
(Workspace-Root)
├── bom/                    Bill of Materials (zentrale Dependency-Verwaltung)
└── root/
    ├── lib/                Wiederverwendbare Bibliotheken (19 Module)
    └── app/
        └── jeeeraaah/      Hauptapplikation
            ├── backend/    OpenLiberty REST API (Port 9080)
            │   ├── api/ws_rs/
            │   └── persistence/
            └── frontend/   JavaFX UI
                ├── api_client/
                └── ui/fx/
```

Build-Einstiegspunkt: `root/pom.xml`

---

## BOM – Dependency-Verwaltung (Pflichtregeln)

**Alle** Dependency-Versionen werden **ausschließlich** im BOM (`bom/pom.xml`) verwaltet.

- In Modul-POMs **niemals** Versionsnummern direkt angeben.
- Neue Dependencies immer zuerst in `bom/pom.xml` als `<dependencyManagement>`-Eintrag anlegen.
- Scope-Ausnahmen (z.B. `test`) dürfen im Modul-POM stehen, die Version nie.
- BOM selbst wird in `root/pom.xml` via `<dependencyManagement>` importiert:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>de.ruu</groupId>
      <artifactId>bom</artifactId>
      <version>${project.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

---

## JPMS – Verbindliche Projektregeln

Dieses Projekt verwendet **konsequent JPMS** (Java Platform Module System). Alle Module haben eine `module-info.java`.

### Grundprinzipien

- `exports` → Compile-time API-Sichtbarkeit (nur öffentliche API exportieren)
- `opens` → Runtime Reflection (nur für Frameworks, immer qualifiziert)
- **Niemals** `opens package;` ohne Modulangabe (außer absolut unvermeidlich)
- **Niemals** Typen nur für Tests exportieren – stattdessen Tests anpassen

### Paketstruktur-Muster

```
my.module/
├── api/          → exports (öffentliche API)
├── internal/     → NICHT exportiert (Implementierung)
└── spi/          → qualified export (nur für bestimmte Frameworks)
```

### Pflicht-Template für module-info.java

```java
module de.ruu.[module.name] {
    // Öffentliche API
    exports de.ruu.[module.name];
    exports de.ruu.[module.name].api;

    // Abhängigkeiten
    requires transitive [api.module];
    requires [impl.module];
    requires static [optional.module];

    // Reflection (minimal, qualifiziert)
    opens de.ruu.[module.name].internal to [framework.x], [framework.z];
}
```

### Scope-Regeln für Maven + JPMS

- Abhängigkeiten, die in `module-info.java` mit `requires` deklariert sind, müssen **`compile` scope** haben (nicht `provided`).
- `provided` scope nur für Abhängigkeiten, die der Container (z.B. Liberty) bereitstellt **und** die **nicht** in `module-info.java` stehen.

### Bekannte Framework-Opens

| Framework  | Modul-Name                       | Zweck                      |
|------------|----------------------------------|----------------------------|
| CDI (Weld) | `weld.core.impl`, `weld.spi`     | Dependency Injection       |
| Jackson    | `com.fasterxml.jackson.databind` | JSON Serialisierung        |
| Lombok     | `lombok`                         | Code-Generierung           |
| Hibernate  | `org.hibernate.orm.core`         | ORM                        |
| JUnit      | `org.junit.platform.commons`     | Tests                      |
| MapStruct  | `org.mapstruct`                  | Mapping (qualified export) |

---

## Infrastruktur & Startup

Das Backend-Ökosystem lebt in **WSL Ubuntu**.

### Docker-Container (alle mit `restart: always`)

| Container             | Port | Datenbank           |
|-----------------------|------|---------------------|
| `postgres-jeeeraaah`  | 5432 | jeeeraaah, lib_test |
| `postgres-keycloak`   | 5433 | Keycloak-DB         |
| `keycloak`            | 8080 | –                   |
| `jasperreports`       | 8090 | –                   |

### Wichtige Aliase (WSL, definiert in `config/shared/wsl/aliases.sh`)

| Alias                    | Aktion                                  |
|--------------------------|-----------------------------------------|
| `ruu-docker-startup`     | Startet alle Container                  |
| `ruu-docker-ps`          | Container-Status                        |
| `ruu-ol-start`           | Backend im Dev-Mode (`mvn liberty:dev`) |
| `ruu-mvn-install`        | `cd root && mvn clean install`          |
| `ruu-mvn-install-fast`   | Build ohne Tests                        |
| `ruu-pg-shell`           | SQL Shell                               |

### Run-Konfigurationen (IntelliJ, in `.run/`)

- **infrastructure – docker startup** → Startet Docker via WSL-Alias
- **backend – liberty:dev** → Startet Backend (Hot-Reload)
- **HierarchiesAppRunner (JPMS)** → JavaFX Frontend (Module Path)
- **DBClean (JPMS)** → DB-Clean Tool

---

## Service-URLs (Entwicklung)

| Service        | URL                                                                        |
|----------------|----------------------------------------------------------------------------|
| Backend API    | http://localhost:9080/jeee-raaah/                                          |
| OpenAPI UI     | http://localhost:9080/openapi/ui                                           |
| Keycloak Admin | http://localhost:8080/admin                                                |
| Keycloak Token | http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token |
| JasperReports  | http://localhost:8090/health                                               |

---

## Dev-Credentials

| Service              | Benutzer    | Passwort       |
|----------------------|-------------|----------------|
| Frontend-Login       | `testuser`  | `testpassword` |
| Keycloak Admin       | `admin`     | `admin`        |
| PostgreSQL jeeeraaah | `jeeeraaah` | `jeeeraaah`    |

---

## Offene Prioritäten (Stand 2026-06-08)

1. **Vollständigkeit:** Keycloak-Kapitel in "JPMS in Action" abschließen
2. **Synchronität:** API-Pfade mit dem Kotlin CMP-Frontend abgleichen
3. **CI/CD:** GitHub Actions aufsetzen
4. **Tests:** 3 Integrationstests aktivieren (benötigen laufendes Backend)

---

## Verwandte Projekte

| Projekt             | Pfad (Windows)                                 | Repo                             |
|---------------------|------------------------------------------------|----------------------------------|
| Kotlin CMP Frontend | `C:\Users\r-uu\develop\github\kotlin\cmp\main` | https://github.com/r-uu/main_cmp |

---

## Weiterführende Dokumentation (im Repo)

| Datei                           | Inhalt                             |
|---------------------------------|------------------------------------|
| `JPMS-REFERENCE.md`             | Vollständige JPMS-Dokumentation    |
| `BUILD-TROUBLESHOOTING.md`      | Bekannte Build-Probleme & Lösungen |
| `IAM-KEYCLOAK-LIBERTY-GUIDE.md` | Keycloak-Setup                     |
| `GETTING-STARTED.md`            | Schnellstart                       |
| `DOCUMENTATION-INDEX.md`        | Vollständiger Doku-Index           |
| `todo.md`                       | Offene Aufgaben                    |
