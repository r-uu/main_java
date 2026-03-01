# IntelliJ Maven Tool Window - Fehlerbehebung

## Problem
Das Maven Tool Window wird in IntelliJ IDEA nicht angezeigt.

## Lösung

### Methode 1: Über das View-Menü
1. Öffnen Sie IntelliJ IDEA
2. Gehen Sie zu: **View** → **Tool Windows** → **Maven**
3. Alternativ: Drücken Sie **Alt + 1** oder **⌘ + 1** (Mac) um die Tool Windows Bar zu öffnen
4. Klicken Sie auf **Maven** in der rechten Seitenleiste

### Methode 2: Über die Tool Windows Bar
1. Klicken Sie auf die **Tool Windows Bar** (normalerweise am linken, rechten oder unteren Rand)
2. Wenn Maven dort nicht erscheint, klicken Sie mit der rechten Maustaste auf die Tool Windows Bar
3. Wählen Sie **Maven** aus der Liste

### Methode 3: Über Quick Access
1. Drücken Sie zweimal **Shift** (Double-Shift) um die "Search Everywhere" zu öffnen
2. Tippen Sie "Maven" ein
3. Wählen Sie **Maven** Tool Window aus der Liste

### Methode 4: Maven-Projekte neu importieren
1. Schließen Sie IntelliJ IDEA komplett
2. Löschen Sie (optional) den `.idea` Ordner und `.iml` Dateien
3. Öffnen Sie IntelliJ IDEA neu
4. Gehen Sie zu: **File** → **Open**
5. Wählen Sie den Projekt-Ordner aus: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main`
6. Wählen Sie **Open as Project**
7. IntelliJ sollte automatisch die Maven-Projekte erkennen und das Maven Tool Window anzeigen

### Methode 5: Maven-Plugin überprüfen
1. Gehen Sie zu: **File** → **Settings** (oder **Ctrl + Alt + S**)
2. Navigieren Sie zu: **Plugins**
3. Suchen Sie nach **Maven**
4. Stellen Sie sicher, dass das **Maven** Plugin aktiviert ist
5. Falls deaktiviert, aktivieren Sie es und starten Sie IntelliJ neu

### Methode 6: Maven-Konfiguration aktualisieren
1. Rechtsklick auf eine `pom.xml` Datei im Projekt (z.B. `root/pom.xml`)
2. Wählen Sie **Add as Maven Project** oder **Maven** → **Reload Project**
3. Das Maven Tool Window sollte automatisch erscheinen

## Konfigurierte Maven-Projekte

Das Projekt enthält folgende Maven-Module:

```
main/
├── bom/pom.xml                    # Bill of Materials (Dependency Management)
└── root/pom.xml                   # Root Aggregator POM
    ├── lib/                       # Libraries
    ├── app/jeeeraaah/            # Main Application
    └── sandbox/                   # Sandbox/Experimental Code
```

## Wichtige Maven-Befehle

Nach dem Öffnen des Maven Tool Windows können Sie:

- **Reload All Maven Projects**: Aktualisiert alle Maven-Abhängigkeiten
- **Generate Sources and Update Folders**: Generiert Sources (z.B. MapStruct)
- **Execute Maven Goal**: Führt Maven-Befehle aus (z.B. `clean install`)
- **Lifecycle**: Zeigt Standard-Maven-Phasen (clean, validate, compile, test, package, install, deploy)

## Verifizierung

Nach erfolgreicher Aktivierung sollten Sie im Maven Tool Window sehen:

```
Maven
├── r-uu.bom (bom)
└── r-uu.root (root)
    ├── r-uu.lib (lib)
    │   ├── r-uu.lib.archunit
    │   ├── r-uu.lib.cdi
    │   ├── r-uu.lib.docker.health
    │   ├── r-uu.lib.fx
    │   ├── ... (weitere lib-Module)
    ├── r-uu.app (app)
    │   └── r-uu.jeeeraaah (jeeeraaah)
    └── r-uu.sandbox (sandbox)
```

## Troubleshooting

### Problem: "Maven project need to be imported"
**Lösung**: Klicken Sie auf "Import Changes" oder verwenden Sie **Ctrl + Shift + O**

### Problem: "No Maven projects found"
**Lösung**: 
1. Überprüfen Sie, ob die `pom.xml` Dateien korrekt sind
2. Überprüfen Sie `.idea/maven.xml` - sollte folgendes enthalten:
```xml
<option name="originalFiles">
  <set>
    <option value="$PROJECT_DIR$/bom/pom.xml" />
    <option value="$PROJECT_DIR$/root/pom.xml" />
  </set>
</option>
```

### Problem: Maven Tool Window ist leer
**Lösung**:
1. Klicken Sie auf das Reload-Symbol (🔄) im Maven Tool Window
2. Oder: Rechtsklick im Maven Tool Window → **Reload All Maven Projects**

## Docker-Umgebung Status

Die Docker-Umgebung wurde erfolgreich neu aufgebaut:

```bash
# Status prüfen
cd ~/develop/github/main/config/shared/docker
./verify-environment.sh

# Oder manuell
docker compose ps
```

**Laufende Services:**
- ✓ PostgreSQL (postgres): localhost:5432
  - jeeeraaah DB: jeeeraaah / jeeeraaah
  - lib_test DB: lib_test / lib_test
  - keycloak DB: keycloak / keycloak
- ✓ Keycloak: localhost:8080
  - Admin: admin / admin
  - Realm: jeeeraaah-realm
  - Test User: jeeeraaah / jeeeraaah

## Nächste Schritte

1. **Maven Tool Window aktivieren** (siehe oben)
2. **Projekt bauen**:
   ```bash
   cd ~/develop/github/main/root
   mvn clean install
   ```
3. **Backend starten**:
   ```bash
   cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
   mvn liberty:dev
   ```
4. **Frontend starten**: 
   - In IntelliJ: Run Configuration "DashAppRunner" ausführen

## Zusätzliche Ressourcen

- IntelliJ IDEA Maven Dokumentation: https://www.jetbrains.com/help/idea/maven-support.html
- Projekt-Dokumentation: `main/config/README.md`
- Quick Start Guide: `main/QUICKSTART.md`

