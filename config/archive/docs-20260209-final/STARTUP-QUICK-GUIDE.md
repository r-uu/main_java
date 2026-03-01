# Jeeeraaah - Schnellstart-Anleitung

**⚡ Starte in 3 Minuten!**

---

## 🚀 Täglicher Start

### 1. Docker Services starten

```bash
ruu-docker-startup   # Startet alle Container + Keycloak Realm Setup
```

**Wartezeit:** ~2-3 Minuten bis alle Container `healthy` sind.

**Hinweis:** Beim ersten Mal nach WSL-Start muss Docker Daemon gestartet werden:
```bash
source ~/.bashrc        # Lädt Aliase (automatisch bei jedem Terminal-Start)
```

### 2. Backend starten (OpenLiberty)

```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```

**Backend-URL:** http://localhost:9080

### 3. Frontend starten (JavaFX)

**In IntelliJ:**
- Run Configuration: `DashAppRunner`
- Klick auf ▶️ Run

**Oder per Kommandozeile:**
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner"
```

---

## 🔧 Bei Problemen

### Container Status prüfen

```bash
ruu-docker-ps
```

Alle Container sollten `healthy` sein:
- ✅ `postgres-jeeeraaah` (Port 5432)
- ✅ `postgres-keycloak` (Port 5433)
- ✅ `keycloak` (Port 8080)
- ✅ `jasperreports` (Port 8090)

### Container neu starten

```bash
ruu-docker-restart
```

### Backend neu starten (nach server.xml Änderungen)

```bash
# Liberty erkennt Änderungen automatisch
# ODER: Strg+C und dann:
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```

### Health Check + Auto-Fix

Die Frontend-Anwendung führt beim Start automatisch Health Checks durch und versucht Probleme zu beheben:

- ❌ Container gestoppt → **Wird automatisch gestartet**
- ❌ Keycloak Realm fehlt → **Wird automatisch erstellt**

Bei Fehlern siehe: [config/DOCKER-AUTO-FIX.md](config/DOCKER-AUTO-FIX.md)

---

## 📝 Häufige Befehle

### Build

```bash
ruu-build                # Gesamtprojekt bauen
ruu-install              # cd root && mvn clean install
ruu-install-fast         # mit -DskipTests
```

### Docker

```bash
ruu-docker-ps            # Status aller Container
ruu-docker-logs          # Logs anzeigen
ruu-docker-restart       # Alle Container neu starten
```

### PostgreSQL

```bash
ruu-postgres-shell       # SQL Shell öffnen (Datenbank: jeeeraaah)
```

### Keycloak

```bash
ruu-keycloak-admin       # Admin Console URL + Credentials anzeigen
```

---

## 🏥 Automatische Health Checks

Beim Start der Frontend-Anwendung werden automatisch geprüft:

1. ✅ Docker Daemon läuft
2. ✅ PostgreSQL Datenbanken erreichbar (`jeeeraaah`, `lib_test`, `keycloak`)
3. ✅ Keycloak Container läuft
4. ✅ Keycloak Realm existiert
5. ✅ JasperReports Service läuft

Bei Fehlern wird **automatisch** versucht zu beheben:
- Container starten
- Realm erstellen

**Details:** [config/DOCKER-AUTO-FIX.md](config/DOCKER-AUTO-FIX.md)

---

## 📚 Weitere Dokumentation

| Link | Beschreibung |
|------|--------------|
| [README.md](README.md) | Vollständige Dokumentation |
| [DOCUMENTATION-INDEX.md](DOCUMENTATION-INDEX.md) | Alle Dokumentationen im Überblick |
| [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md) | Problemlösungen |

---

## ✅ Checkliste: Alles läuft

- [ ] `ruu-docker-ps` zeigt alle 4 Container als `healthy`
- [ ] Backend erreichbar: http://localhost:9080/health
- [ ] Keycloak Admin Console: http://localhost:8080/admin
- [ ] Frontend startet ohne Fehler
- [ ] Login funktioniert (automatisch in Test-Modus)

**🎉 Wenn alle Punkte ✅, dann läuft alles perfekt!**

---

**Letzte Aktualisierung:** 2026-01-23
