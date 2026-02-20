# ✅ GanttApp Start-Checkliste

## Quick Start (3 Schritte)

### 1️⃣ Verifikation (empfohlen)
```bash
cd ~/develop/github/main
chmod +x verify-ganttapp-fix.sh
./verify-ganttapp-fix.sh
```

**Erwartung:** "✅ ALLE VERIFIKATIONS-TESTS BESTANDEN!"

### 2️⃣ Docker Container starten (falls nicht läuft)
```bash
cd ~/develop/github/main/config/shared/docker
docker compose up -d
```

**Prüfen:**
```bash
docker ps | grep -E "postgres|keycloak"
```

### 3️⃣ GanttApp starten
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

**Erwartetes Ergebnis:**
```
✅ Configuration properties validated successfully
✅ Docker environment health check passed
=== Testing mode enabled - attempting automatic login ===
  Test credentials found: username=test
  ✅ Automatic login successful
```

---

## Bei Problemen

### Problem: Verifikation schlägt fehl
**Lösung:** Führe den kompletten Fix aus
```bash
cd ~/develop/github/main
chmod +x fix-ganttapp-complete.sh
./fix-ganttapp-complete.sh
```

### Problem: Keycloak nicht erreichbar
**Lösung:** Docker Container starten
```bash
cd ~/develop/github/main/config/shared/docker
docker compose up -d
docker ps
```

### Problem: Login schlägt immer noch fehl
**Lösung:** Keycloak Realm Setup ausführen
```bash
cd ~/develop/github/main/root/lib/keycloak.admin
mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

---

## Was wurde gefixt?

✅ **Hauptproblem:** `config.file.name` verwendet jetzt absoluten Pfad
- Vorher: `config.file.name=testing.properties` (Datei nicht gefunden!)
- Nachher: `config.file.name=/home/r-uu/develop/github/main/testing.properties` (funktioniert!)

✅ **Keycloak Properties:** Sind jetzt in microprofile-config.properties
- `keycloak.test.user=test`
- `keycloak.test.password=test`

✅ **Keycloak User:** Test-User `test/test` wurde angelegt

✅ **Build:** Frontend UI FX Modul wurde neu gebaut und installiert

---

## Weitere Hilfe

- `README-GANTTAPP-FIX.md` - Hauptdokumentation mit Details
- `FILES-OVERVIEW.md` - Übersicht aller Fix-Dateien und Skripte
- `KEYCLOAK-LOGIN-FIX.md` - Ausführliche technische Anleitung

---

**Status:** ✅ Problem behoben - GanttApp ist startbereit!

