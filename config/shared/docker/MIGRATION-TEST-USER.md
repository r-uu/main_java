# ✅ Keycloak Test User Migration zu test/test

## Durchgeführte Änderungen

### 1. Credentials Dokumentation aktualisiert
- **Datei:** `config/shared/docker/CREDENTIALS.md`
- **Änderung:** Keycloak Test User auf `test/test` aktualisiert
- **Status:** ✅ Abgeschlossen

### 2. Testing Properties (bereits korrekt)
- **Datei:** `testing.properties` (Projekt-Root)
- **Konfiguration:**
  ```properties
  testing.username=test
  testing.password=test
  keycloak.test.user=test
  keycloak.test.password=test
  ```
- **Status:** ✅ Bereits korrekt

### 3. Keycloak Realm Setup (bereits korrekt)
- **Datei:** `root/lib/keycloak_admin/src/main/java/.../KeycloakRealmSetup.java`
- **Konfiguration:**
  ```java
  private static final String TEST_USER = "test";
  private static final String TEST_PASSWORD = "test";
  ```
- **Status:** ✅ Bereits korrekt

### 4. Test-Skript erstellt
- **Datei:** `config/shared/docker/TEST-KEYCLOAK-LOGIN.sh`
- **Funktion:** Testet Keycloak Login mit test/test
- **Status:** ✅ Neu erstellt

## Verwendung

### Automatisches Login in DashAppRunner
Der DashAppRunner liest die Credentials aus `testing.properties`:
- Wenn `testing=true`, wird automatisch mit `test/test` eingeloggt
- Keine manuelle Eingabe erforderlich

### Manuelles Testen
```bash
# Keycloak Login testen
cd ~/develop/github/main/config/shared/docker
./TEST-KEYCLOAK-LOGIN.sh
```

### Keycloak Realm Setup (bei Bedarf)
```bash
# Realm und User erstellen/aktualisieren
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

## Verifizierung

### 1. Container Status prüfen
```bash
docker ps
# Erwartung: postgres, keycloak, jasperreports alle healthy
```

### 2. Keycloak Health prüfen
```bash
curl http://localhost:8080/health/ready
# Erwartung: HTTP 200
```

### 3. Login testen
```bash
./TEST-KEYCLOAK-LOGIN.sh
# Erwartung: ✅ Login erfolgreich
```

### 4. DashAppRunner starten
```bash
# In IntelliJ: Run Configuration "DashAppRunner (JPMS)"
# Erwartung: Automatisches Login mit test/test
```

## Credentials Übersicht

### PostgreSQL
| Schema     | User      | Password  | Port |
|------------|-----------|-----------|------|
| jeeeraaah  | jeeeraaah | jeeeraaah | 5432 |
| lib_test   | lib_test  | lib_test  | 5432 |
| keycloak   | keycloak  | keycloak  | 5432 |

### Keycloak
| Bereich        | User  | Password | Realm            |
|----------------|-------|----------|------------------|
| Admin Console  | admin | admin    | master           |
| Test User      | test  | test     | jeeeraaah-realm  |

## Nächste Schritte

1. ✅ Docker Container starten
2. ✅ Keycloak Realm Setup ausführen
3. ✅ Login mit TEST-KEYCLOAK-LOGIN.sh testen
4. ✅ DashAppRunner in IntelliJ starten
5. ✅ Verifizieren, dass automatisches Login funktioniert

## Troubleshooting

### Problem: Login schlägt fehl mit "invalid_grant"
**Lösung:** Keycloak Realm Setup ausführen
```bash
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

### Problem: Keycloak Container nicht healthy
**Lösung:** Container neu starten
```bash
cd ~/develop/github/main/config/shared/docker
docker compose down
docker compose up -d
```

### Problem: DashAppRunner zeigt Fehlermeldung
**Lösung:** Prüfe testing.properties
```bash
# Stelle sicher, dass folgende Properties gesetzt sind:
testing=true
keycloak.test.user=test
keycloak.test.password=test
```

---

**Datum:** 2026-01-30  
**Status:** ✅ Abgeschlossen  
**Nächster Schritt:** Container starten und testen
