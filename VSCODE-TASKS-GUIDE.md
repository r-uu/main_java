# VS Code Tasks & Shortcuts - Liberty Development

**Erstellt:** 2026-03-01  
**Zweck:** Komfortable Liberty-Steuerung direkt aus VS Code

---

## 🚀 Schnellstart

### Methode 1: Command Palette (Empfohlen für Einsteiger)

1. **`Ctrl+Shift+P`** - Command Palette öffnen
2. **"Tasks: Run Task"** eingeben
3. Task auswählen:
   - **🚀 Liberty: Start (Dev Mode)** - Liberty im Dev-Modus starten
   - **🛑 Liberty: Stop** - Liberty stoppen
   - **🎨 Frontend: Start DashApp** - JavaFX Frontend starten

### Methode 2: Keyboard Shortcuts (Schnellste Methode!) ⚡

| Shortcut | Aktion | Beschreibung |
|----------|--------|--------------|
| **`Ctrl+Shift+L`** | Liberty Start | Startet Liberty im Dev-Modus |
| **`Ctrl+Shift+K`** | Liberty Stop | Stoppt Liberty |
| **`Ctrl+Shift+D`** | Frontend Start | Startet DashApp (JavaFX) |
| **`Ctrl+Shift+B`** | Build | Vollständiger Maven Build |

### Methode 3: Terminal Menu

1. **Terminal** → **Run Task...**
2. Task auswählen

---

## 📋 Verfügbare Tasks

### Backend (Liberty)

#### 🚀 Liberty: Start (Dev Mode)
```bash
mvn liberty:dev
```
- **Was:** Startet Open Liberty im Development-Modus
- **Features:**
  - Hot Reload bei Code-Änderungen
  - Automatisches Deployment
  - Debug-Port 7777 aktiv
- **Panel:** Eigenes dediziertes Terminal
- **Shortcut:** `Ctrl+Shift+L`

#### 🛑 Liberty: Stop
```bash
mvn liberty:stop
```
- **Was:** Stoppt laufende Liberty-Instanz
- **Wann:** Nach Dev-Session oder vor erneutem Start
- **Shortcut:** `Ctrl+Shift+K`

#### ▶️ Liberty: Run (Production Mode)
```bash
mvn liberty:run
```
- **Was:** Startet Liberty im Produktions-Modus
- **Unterschied zu Dev:** Kein Hot Reload, optimiert für Performance
- **Wann:** Für finale Tests vor Deployment

---

### Frontend

#### 🎨 Frontend: Start DashApp
```bash
mvn exec:java
```
- **Was:** Startet JavaFX DashApp über Maven
- **JPMS:** Korrekte Module-Path-Konfiguration
- **Shortcut:** `Ctrl+Shift+D`

---

### Build & Infrastructure

#### 🔨 Build: Full Project
```bash
mvn clean install -DskipTests
```
- **Was:** Kompletter Projekt-Build ohne Tests
- **Wann:** Nach größeren Änderungen
- **Shortcut:** `Ctrl+Shift+B` (Default Build Task)

#### 🐳 Docker: Start All
```bash
bash startup-and-setup.sh
```
- **Was:** Startet PostgreSQL, Keycloak und alle Docker-Services
- **Wartet:** Bis alle Services bereit sind

#### 🐳 Docker: Stop All
```bash
docker compose down
```
- **Was:** Stoppt alle Docker-Container

---

## 🎯 Typischer Workflow

### 1. Projekt starten (Morgens)
```
Ctrl+Shift+P → "Docker: Start All" → Enter
   └─ Wartet bis Docker bereit (Postgres + Keycloak)

Ctrl+Shift+L  (Liberty starten)
   └─ Wartet bis Backend läuft (http://localhost:9080)

Ctrl+Shift+D  (Frontend starten)
   └─ DashApp startet (Login mit Keycloak)
```

### 2. Entwicklung (Tagsüber)
- Code ändern → Liberty erkennt automatisch (Hot Reload)
- Bei JPMS-Änderungen: `Ctrl+Shift+K` → `Ctrl+Shift+L` (Liberty neu starten)

### 3. Projekt beenden (Abends)
```
Ctrl+Shift+K  (Liberty stoppen)
Fenster schließen für Frontend
Ctrl+Shift+P → "Docker: Stop All" → Enter
```

---

## ☀️ Arbeiten im Freien / bei Sonnenschein

Die Tasks sind für **ungestörtes Arbeiten** optimiert:

**Terminal-Verhalten:**
- ✅ **`reveal: "silent"`** - Terminal öffnet sich nur bei Fehlern
- ✅ **`focus: false`** - Editor bleibt fokussiert
- ✅ **Panel bleibt im Hintergrund** - keine Ablenkung

**Liberty läuft, Terminal bleibt unsichtbar:**
1. `Ctrl+Shift+L` - Liberty startet im Hintergrund
2. Arbeite konzentriert im Editor
3. Bei Bedarf: `Ctrl+J` → Terminal-Panel öffnen

**Terminal wieder anzeigen:**
```
Ctrl+J                → Panel öffnen/schließen
Ctrl+` (Backtick)     → Terminal-Panel togglen
```

**Status prüfen ohne Terminal:**
- Liberty-Logs: Nur bei Fehlern automatisch sichtbar
- Manuell prüfen: `Ctrl+J` → Terminal auswählen → Logs lesen

**Für bessere Lesbarkeit bei Sonnenlicht:**
1. `Ctrl+K Ctrl+T` → Theme wechseln
2. Empfohlen: **"GitHub Light"** oder **"Quiet Light"**
3. Terminal-Schrift größer: Settings → `terminal.integrated.fontSize: 16`

---

## 💡 Tipps & Tricks

### Liberty Dev-Modus Features

**Hot Reload funktioniert für:**
- ✅ Java-Klassen (automatisch)
- ✅ REST-Endpoints (automatisch)
- ✅ server.xml Änderungen (automatisch)
- ❌ module-info.java (Neustart erforderlich)
- ❌ POM-Änderungen (Neustart erforderlich)

**Liberty-Logs überwachen:**
- Terminal ist automatisch sichtbar
- Suche in Logs: `Ctrl+F` im Terminal

**Liberty Debug-Port:**
- Port: 7777
- In VS Code: Run & Debug → "Attach to Liberty"

### Terminal-Organisation

**Panel-Strategie:**
- Liberty → Eigenes dediziertes Panel (bleibt offen)
- Frontend → Eigenes dediziertes Panel (bleibt offen)
- Build/Docker → Shared Panel (wird wiederverwendet)

**Mehrere Terminals:**
1. Mehrere Tasks parallel starten
2. Jeder Task mit `isBackground: true` bekommt eigenes Terminal
3. Zwischen Terminals wechseln: Terminal-Dropdown

---

## 🔧 Anpassungen

### Eigene Shortcuts definieren

Bearbeite `.vscode/keybindings.json`:
```json
{
  "key": "ctrl+alt+l",  // Dein eigener Shortcut
  "command": "workbench.action.tasks.runTask",
  "args": "🚀 Liberty: Start (Dev Mode)"
}
```

### Task-Konfiguration anpassen

Bearbeite `.vscode/tasks.json`:
```json
{
  "label": "Mein Custom Task",
  "command": "mvn",
  "args": ["clean", "install"],
  "options": {
    "cwd": "${workspaceFolder}"
  }
}
```

---

## 🐛 Troubleshooting

### "Task is already active"
**Problem:** Liberty läuft bereits  
**Lösung:** `Ctrl+Shift+K` (stoppen) → dann erneut starten

### Liberty startet nicht
**Lösung 1:** Docker services laufen?  
```bash
docker ps  # PostgreSQL und Keycloak müssen laufen
```

**Lösung 2:** Port 9080 bereits belegt?  
```bash
netstat -tulpn | grep 9080
```

**Lösung 3:** Neustart erzwingen  
```bash
Ctrl+Shift+K  (stoppen)
Terminal schließen
Ctrl+Shift+L  (erneut starten)
```

### Shortcuts funktionieren nicht
**Ursache:** `.vscode/keybindings.json` wird nicht automatisch geladen  
**Lösung:** VS Code neu starten (`Ctrl+Shift+P` → "Reload Window")

---

## 📚 Weiterführende Dokumentation

- [VSCODE-ERRORS-EXPLAINED-2026-03-01.md](../VSCODE-ERRORS-EXPLAINED-2026-03-01.md) - VS Code Fehlerbehandlung
- [IAM-KEYCLOAK-LIBERTY-GUIDE.md](../IAM-KEYCLOAK-LIBERTY-GUIDE.md) - Liberty & Keycloak Setup
- [config/shared/wsl/aliases.sh](../config/shared/wsl/aliases.sh) - Terminal-Aliase (Alternative zu Tasks)

---

## 🎓 Alternative: Terminal-Aliase

Falls du lieber im integrierten Terminal arbeitest:

```bash
# Aliase laden (einmalig)
source config/shared/wsl/aliases.sh

# Liberty starten
ruu-liberty-start

# Liberty stoppen
ruu-liberty-stop

# Frontend starten
ruu-dash
```

**Vorteil Tasks vs. Aliase:**
- Tasks: Schöne UI, Shortcuts, Panel-Management
- Aliase: Schneller Zugriff, flexibler, scriptbar

**Empfehlung:** Verwende Tasks für Entwicklung, Aliase für Scripting/CI.

---

*Letzte Aktualisierung: 2026-03-01*
