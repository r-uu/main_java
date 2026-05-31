# JEEERAAAH - Java Enterprise Task Management

**GraalVM 25 • JavaFX 25 • Jakarta EE 10 • JPMS • Microservices-Ready**

---

## 🎯 START HERE

**👉 Für den schnellsten Einstieg: [GETTING-STARTED.md](GETTING-STARTED.md)**

**📚 Vollständiger Dokumentations-Index: [DOCUMENTATION-INDEX.md](DOCUMENTATION-INDEX.md)**

---

## ⚡ Ultra-Schnellstart

```bash
# 1. Container starten (alle Services)
source ~/.bashrc
ruu-docker-startup

# 2. Warte 2-3 Minuten

# 3. Projekt bauen
cd /home/r-uu/develop/github/java/main/root
mvn clean install
```

**Das war's!** ✅

---

## 📚 Wichtigste Dokumentationen

| Dokument                                      | Beschreibung |
|-----------------------------------------------|--------------|
| **[quick-start](GETTING-STARTED.md)**         | ⭐ **Schnellstart-Anleitung** |
| [doku-index](DOCUMENTATION-INDEX.md)          | 📚 Vollständiger Dokumentations-Index |
| [docker](config/shared/docker/README.md)      | 🐳 Docker Setup & Container |
| [docker-health](DOCKER-HEALTH-CHECK-FIX.md)   | 🏥 Health Check & Auto-Fix System |
| [keycloak](root/lib/keycloak_admin/README.md) | 🔐 Keycloak Setup & Management |

---

## 🐳 Docker Container (Autostart: ✅)

**Alle Container starten automatisch** mit `restart: always`:

- `postgres-jeeeraaah` (Port 5432) - Datenbanken: **jeeeraaah** + **lib_test**
- `postgres-keycloak` (Port 5433) - Keycloak-Datenbank
- `keycloak` (Port 8080) - Identity & Access Management
- `jasperreports` (Port 8090) - Report-Service

**Status prüfen:**
```bash
ruu-docker-ps
```

---

## 🔧 Wichtigste Befehle (Aliase)

### Container
```bash
ruu-docker-startup       # Kompletter Startup (empfohlen!)
ruu-docker-ps            # Container-Status
ruu-docker-logs          # Alle Logs anzeigen
ruu-docker-restart       # Alle Container neu starten
```

### Build
```bash
ruu-mvn-build            # Projekt komplett bauen
ruu-mvn-install          # cd root && mvn clean install
ruu-mvn-install-fast     # mvn clean install -DskipTests
```

### PostgreSQL
```bash
ruu-pg-shell              # SQL Shell öffnen
ruu-pg-ensure-lib-test    # lib_test Datenbank prüfen/erstellen
```

### Keycloak
```bash
ruu-kc-admin        # Admin-URL anzeigen
ruu-kc-setup        # Realm einrichten
```

### Hilfe
```bash
ruu-help                # Alle Aliase anzeigen
ruu-versions            # Tool-Versionen prüfen
```

**Mehr:** `config/shared/wsl/aliases.sh`

---

## 🏃 Anwendung starten

### Backend (Liberty Server):
```bash
cd /home/r-uu/develop/github/java/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```

**Backend URLs:**
- API: http://localhost:9080
- OpenAPI: http://localhost:9080/openapi/ui/
- Health: http://localhost:9080/health/

### Frontend (JavaFX Desktop):

**IntelliJ:** Run Configuration `HierarchiesAppRunner`

**Oder manuell:**
```bash
cd /home/r-uu/develop/github/java/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.hierarchy.HierarchiesAppRunner"
```

---

## 🏗️ PROJEKT-STRUKTUR

```
main/
├── bom/                          # Bill of Materials (zentrale Dependency-Verwaltung)
├── root/
│   ├── app/
│   │   └── jeeeraaah/
│   │       ├── backend/          # OpenLiberty REST API (Port 9080)
│   │       │   ├── api/ws_rs/    # REST Endpoints
│   │       │   └── persistence/  # JPA Entities
│   │       └── frontend/         # JavaFX UI
│   │           ├── api_client/   # REST Client
│   │           └── ui/fx/        # HierarchiesAppRunner (JavaFX)
│   ├── lib/                      # Wiederverwendbare Libraries
│   │   ├── keycloak_admin/       # Keycloak Setup & Management
│   │   ├── jpa/                  # JPA Core
│   │   └── office/word/          # Dokument-Generierung
│   │       ├── docx4j/           # DOCX4J Implementation
│   │       └── jasperreports/    # JasperReports Service
│   └── sandbox/                  # Experimente & Prototypen
└── config/
    └── shared/
        ├── docker/               # Docker Compose Konfiguration
        ├── scripts/              # Build & Setup Skripte
        └── wsl/                  # WSL-spezifische Konfiguration
```

---

## 🔧 TECHNOLOGIE-STACK

### Backend
- **Application Server:** OpenLiberty
- **REST API:** JAX-RS (Jakarta EE)
- **Persistence:** JPA (Hibernate via Liberty)
- **Database:** PostgreSQL 16
- **Authentication:** Keycloak (OAuth2/OpenID Connect)
- **API Documentation:** OpenAPI 3.x

### Frontend
- **UI Framework:** JavaFX 25
- **Dependency Injection:** CDI (Weld SE)
- **REST Client:** Jersey Client
- **Authentication:** Keycloak Direct Access Grants

### Build & Runtime
- **JDK:** GraalVM 25 (Oracle)
- **Build:** Maven 3.9.x
- **Module System:** JPMS (Java Platform Module System)
- **Logging:** Log4j2

---

## 📋 WICHTIGE BEFEHLE

### Build

```bash
# Gesamtprojekt bauen
cd /home/r-uu/develop/github/java/main/root
mvn clean install

# Nur Backend bauen
cd /home/r-uu/develop/github/java/main/root/app/jeeeraaah/backend/api/ws_rs
mvn clean package
```

### Docker

```bash
# Container starten
docker compose up -d

# Container stoppen
docker compose down

# Container + Volumes löschen (Reset)
docker compose down -v

# Container Status
docker ps
```

### Keycloak

```bash
# Setup ausführen
cd /home/r-uu/develop/github/java/main/root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"

# Admin Console
# URL: http://localhost:8080/admin
# Login: admin / admin
```

### Backend (OpenLiberty)

```bash
# Dev-Modus (Hot Reload)
cd /home/r-uu/develop/github/java/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev

# Nur starten
mvn liberty:run

# Stoppen
mvn liberty:stop
```

---

## 🐛 TROUBLESHOOTING

### Problem: Backend kompiliert nicht (Byte Buddy Fehler)

**Fehler:**
```
Java 25 is not supported by the current version of Byte Buddy
```

**Lösung:** ✅ **BEREITS BEHOBEN!** 

Byte Buddy wurde auf Version **1.18.4** aktualisiert in `bom/pom.xml`.

**Hinweis:** 1.18.4 ist die neueste stabile Version (Stand: Januar 2026) und unterstützt Java 25 vollständig.

### Problem: Keycloak Login fehlschlägt

**Fehler:**
```
invalid_client
```

**Lösung:**
1. Keycloak Setup erneut ausführen (siehe oben)
2. Prüfe dass Direct Access Grants aktiviert ist:
   - Admin Console → realm_default → Clients → jeeeraaah-frontend
   - "Direct Access Grants Enabled" = ON

### Problem: Frontend kann Backend nicht erreichen

**Fehler:**
```
Connection refused (Port 9080)
```

**Lösung:** Backend starten (siehe Schritt 3 oben)

### Problem: Datenbank-Verbindung fehlschlägt

**Lösung:**
```bash
# Container neustarten
cd /home/r-uu/develop/github/java/main/config/shared/docker
docker compose restart postgres-jeeeraaah

# Logs prüfen
docker logs postgres-jeeeraaah
```

### Problem: Port bereits belegt

```bash
# Prüfe welcher Prozess Port nutzt
sudo lsof -i:9080    # Backend
sudo lsof -i:8080    # Keycloak
sudo lsof -i:5432    # PostgreSQL
```

---

## 🔑 WICHTIGE URLs

| Service              | URL                                          | Credentials     |
|----------------------|----------------------------------------------|-----------------|
| **Keycloak Admin**   | http://localhost:8080/admin                  | admin / admin   |
| **Keycloak Realm**   | http://localhost:8080/realms/jeeeraaah-realm | -               |
| **Backend API**      | http://localhost:9080/jeee-raaah/            | -               |
| **Backend Health**   | http://localhost:9080/health                 | -               |
| **JasperReports**    | http://localhost:8090/health                 | -               |

---

## 📝 KONFIGURATION

### Lokale Konfigurationsdateien

**NICHT unter Git-Kontrolle:**
- `config.properties` (Root-Level)
- `testing.properties` (Frontend)
- Enthalten maschinenspezifische Werte (DB-Credentials, etc.)

### Wichtige Properties

**Datenbank (PostgreSQL):**
```properties
db.host=localhost
db.port=5432
db.name=lib_test
db.user=r_uu
db.password=r_uu_password
```

**Keycloak:**
```properties
keycloak.url=http://localhost:8080
keycloak.realm=jeeeraaah-realm
keycloak.client=jeeeraaah-frontend
```

**Backend REST API:**
```properties
jeeeraaah.rest-api.scheme=http
jeeeraaah.rest-api.host=localhost
jeeeraaah.rest-api.port=9080
```

---

## 🎯 ENTWICKLUNGS-WORKFLOW

### 1. Täglich

```bash
# 1. Docker Container starten (falls nicht laufen)
docker compose up -d

# 2. Backend starten
cd /home/r-uu/develop/github/java/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev

# 3. Frontend in IntelliJ starten
# Run → HierarchiesAppRunner
```

### 2. Nach Git Pull

```bash
# Dependencies aktualisieren
cd /home/r-uu/develop/github/java/main/root
mvn clean install
```

### 3. Bei Container-Problemen

```bash
# Container komplett reset
cd /home/r-uu/develop/github/java/main/config/shared/docker
docker compose down -v
docker compose up -d

# Keycloak Realm neu erstellen
cd /home/r-uu/develop/github/java/main/root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

---

## 📚 WEITERFÜHRENDE DOKUMENTATION

- [**Backend API Details**](root/app/jeeeraaah/backend/api/ws_rs/README.md)
- [**Keycloak Setup**](IAM-KEYCLOAK-LIBERTY-GUIDE.md)
- [**Docker Setup:**](config/shared/docker/README.md)

---

## ✅ CHECKLISTE: ALLES LÄUFT

- [ ] Docker Container: Alle 4 Container sind **healthy**
- [ ] Keycloak: Admin Console erreichbar (http://localhost:8080/admin)
- [ ] Backend: Health Check erfolgreich (http://localhost:9080/health)
- [ ] Frontend: HierarchiesAppRunner startet ohne Fehler
- [ ] Login: Automatischer Login funktioniert

**🎉 WENN ALLE PUNKTE ✅, DANN LÄUFT ALLES!**

---

# NEXT STEPS

## IMPLEMENTIERUNG WEITERER FEATURES

### 

## Code-Qualität verbessern (z.B. Logging, Fehlerbehandlung)

## Tests hinzufügen

## Dokumentation erweitern...