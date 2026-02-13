# Empfohlene Verbesserungen - Priorisierte Liste
**Stand:** 2026-02-13  
**Status:** Nach Mapping-Reorganisation
---
## ✅ Gerade abgeschlossen
- ✅ **JavaFX auf Version 25 aktualisiert**
- ✅ **Mapping-Module reorganisiert** (frontend/backend/common Struktur)
- ✅ **Module-info.java Javadoc hinzugefügt**
- ✅ **Recursion Guards geprüft** (sind notwendig, bleiben erhalten)
---
## 🔥 Priority 1 - Sofort (Heute/Diese Woche)
### 1. Module-info.java Dokumentation vervollständigen
**Status:** Teilweise erledigt  
**Aufwand:** 1-2 Stunden  
**Nutzen:** Bessere Code-Dokumentation, API-Klarheit
**Aktion:**
- Alle `module-info.java` Dateien mit Javadoc versehen
- Erklären, was jedes Modul macht und warum
- Dependencies dokumentieren
**Kommando:**
```bash
find root/*/module-info.java -type f
# Dann für jedes Modul Javadoc hinzufügen
```
---
### 2. Compiler Warnings beheben
**Status:** Offen  
**Aufwand:** 2-3 Stunden  
**Nutzen:** Sauberer Build, potenzielle Bugs vermeiden
**Bekannte Warnings:**
- Lombok `@NonNull` nicht transitiv exportiert
- Unbenutzte Parameter in einigen Mappern
- Potenzielle null-pointer issues
**Aktion:**
```bash
mvn clean compile 2>&1 | grep WARNING | sort | uniq
```
---
### 3. Dokumentations-Konsolidierung
**Status:** Teilweise (viel in archive/)  
**Aufwand:** 3-4 Stunden  
**Nutzen:** Einfacherer Einstieg für Entwickler
**Zu konsolidieren:**
- ❌ 3 Startup-Guides → 1 einheitlicher Guide
- ❌ 3 Credentials-Dokumente → 1 zentrales Dokument  
- ❌ Deutsche Kommentare → Englisch übersetzen
- ✅ Archive bereinigt (schon erledigt)
**Dateien:**
- `QUICKSTART.md` + `GETTING-STARTED.md` + `STARTUP-QUICK-GUIDE.md` → `GETTING-STARTED.md`
- Credentials: `config/CREDENTIALS.md` + `config/JWT-TROUBLESHOOTING.md` + `config/KEYCLOAK-ADMIN-CONSOLE.md`
---
## 📋 Priority 2 - Wichtig (Diese Woche)
### 4. Tests hinzufügen
**Status:** Lückenhaft  
**Aufwand:** Kontinuierlich  
**Nutzen:** Robusterer Code, weniger Regressions
**Bereiche:**
- Unit-Tests für Mapper (MapStruct)
- Integration-Tests für REST API
- JavaFX Controller Tests
**Aktion:**
```bash
# Test-Coverage prüfen
mvn clean test jacoco:report
```
---
### 5. ArchUnit Regeln erweitern
**Status:** Basis vorhanden  
**Aufwand:** 2-3 Stunden  
**Nutzen:** Architektur-Konsistenz erzwingen
**Regeln hinzufügen:**
- `lib/*` darf nicht von `app/*` abhängen
- Layer-Abhängigkeiten prüfen (UI → Service → Persistence)
- Naming-Konventionen prüfen
---
### 6. Docker Health Checks testen
**Status:** Implementiert, aber nicht getestet  
**Aufwand:** 1 Stunde  
**Nutzen:** Zuverlässigere Container-Starts
**Aktion:**
```bash
# Alle Container stoppen und neu starten
docker-compose down
docker-compose up -d
# Health-Checks beobachten
docker-compose ps
```
---
## 🚀 Priority 3 - Empfohlen (Diesen Monat)
### 7. CI/CD Pipeline einrichten
**Status:** Nicht vorhanden  
**Aufwand:** 4-6 Stunden  
**Nutzen:** Automatisierte Qualitätssicherung
**Schritte:**
1. GitHub Actions / GitLab CI Konfiguration
2. Build bei jedem Push
3. Tests automatisch ausführen
4. Dependency-Check (OWASP)
**Beispiel `.github/workflows/build.yml`:**
```yaml
name: Build and Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '25'
      - run: mvn clean verify
```
---
### 8. Code-Qualität Tools
**Status:** Nicht konfiguriert  
**Aufwand:** 3-4 Stunden  
**Nutzen:** Automatische Code-Review
**Tools hinzufügen:**
- **SpotBugs** (Bugs finden)
- **PMD** (Code-Smells)
- **Checkstyle** (Style-Guide)
- **JaCoCo** (Test-Coverage)
**pom.xml erweitern:**
```xml
<plugin>
  <groupId>com.github.spotbugs</groupId>
  <artifactId>spotbugs-maven-plugin</artifactId>
  <version>4.8.3.1</version>
</plugin>
```
---
### 9. API-Dokumentation generieren
**Status:** Nicht vorhanden  
**Aufwand:** 2-3 Stunden  
**Nutzen:** Bessere API-Nutzbarkeit
**Tools:**
- OpenAPI/Swagger für REST API
- Javadoc für Java-Bibliotheken
**Aktion:**
```bash
# Javadoc generieren
mvn javadoc:aggregate
# OpenAPI nutzt bereits MicroProfile OpenAPI Annotations
# Swagger UI hinzufügen
```
---
## 💡 Priority 4 - Nice to Have (Nächstes Quartal)
### 10. Performance Optimierung
- Startup-Zeit profilen
- Docker-Images optimieren (Multi-stage builds)
- Database-Queries optimieren (N+1 Problem)
### 11. Security Hardening
- Dependency Vulnerability Scanning
- Security Headers (CORS, CSP, etc.)
- Input Validation verschärfen
- Secret Management (Vault, etc.)
### 12. Monitoring & Observability
- Micrometer Metrics
- Prometheus Endpoint
- Structured Logging (JSON)
- Distributed Tracing (OpenTelemetry)
---
## 🎯 Empfohlene Reihenfolge (Nächste 2 Wochen)
1. **Tag 1-2:** Module-info.java Javadoc vervollständigen
2. **Tag 2-3:** Compiler Warnings beheben
3. **Tag 3-5:** Dokumentation konsolidieren
4. **Tag 6-7:** Tests hinzufügen (priority areas)
5. **Tag 8-9:** ArchUnit Regeln erweitern
6. **Tag 10:** Docker Health Checks testen
7. **Woche 2:** CI/CD Pipeline + Code-Quality Tools
---
## 📊 Metriken zum Tracken
- **Compiler Warnings:** Aktuell ~20 → Ziel: 0
- **Test Coverage:** Aktuell unbekannt → Ziel: >70%
- **Dokumentations-Dateien:** 50+ → Ziel: <20 (konsolidiert)
- **Build-Zeit:** Messen und optimieren
- **Docker-Startup-Zeit:** Messen und optimieren
---
## 🤔 Fragen zu klären
1. Soll JasperReports aktiviert bleiben? (Dockerfile fehlt)
2. Welche CI/CD Plattform? (GitHub Actions, GitLab CI, Jenkins)
3. Monitoring-Stack gewünscht? (Prometheus + Grafana)
4. Deployment-Strategie? (Docker, Kubernetes, VM)
