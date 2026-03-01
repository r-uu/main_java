# 🔧 JWT Authorization Troubleshooting

## ❌ PROBLEM: Unauthorized (403) beim ersten Request

### Symptome

**Server (Liberty):**
```
jakarta.ws.rs.ForbiddenException: Unauthorized
at io.openliberty.restfulWS30.appSecurity.LibertyAuthFilter.handleMessage
```

**Client:**
```
de.ruu.lib.ws.rs.TechnicalException: failed to retrieve task groups: 
{"message":"INTERNAL_ERROR","cause":"Unauthorized","httpStatus":"INTERNAL_SERVER_ERROR"}
```

---

## 🔍 DIAGNOSE-SCHRITTE

### 1. Prüfe: Läuft automatisches Login?

**Im DashAppRunner Log suchen:**
```
✅ Automatic login successful
Access token (first 50 chars): eyJhbGciOi...
```

**Wenn NICHT vorhanden:**
- → [FRONTEND-SESSION-EXPIRED-FIX.md](FRONTEND-SESSION-EXPIRED-FIX.md)
- Prüfe `testing.properties`
- Prüfe Keycloak Container ist healthy

---

### 2. Prüfe: Wird Authorization Header gesendet?

**Im DashAppRunner Log suchen:**
```
=== AuthorizationHeaderFilter called ===
  isLoggedIn(): true
  Token present: true
  ✅ Authorization header added
```

**Wenn "isLoggedIn(): false":**
- Token ist abgelaufen
- Erneut login erforderlich
- → DashAppRunner neu starten

**Wenn "Token present: false":**
- Bug im KeycloakAuthService
- → Check KeycloakAuthService.accessToken

---

### 3. Prüfe: Hat Liberty das groupNameAttribute?

**Command:**
```bash
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
grep "groupNameAttribute" target/liberty/wlp/usr/servers/defaultServer/server.xml
```

**Erwartete Ausgabe:**
```xml
groupNameAttribute="realm_access/roles">
```

**Wenn NICHT vorhanden:**
- → [JWT-AUTHORIZATION-FIX.md](JWT-AUTHORIZATION-FIX.md)
- `server.xml` aktualisieren
- Liberty Config Reload:
  ```bash
  touch src/main/liberty/config/server.xml
  ```

---

### 4. Prüfe: Hat Liberty die Config neu geladen?

**In Liberty Logs suchen:**
```
CWWKG0017I: The server configuration was successfully updated
```

**Wenn NICHT vorhanden:**
```bash
# Force reload
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
touch src/main/liberty/config/server.xml

# Warte 10 Sekunden, dann nochmal prüfen
```

---

## ✅ LÖSUNG: Vollständiger Fix-Ablauf

### Schritt 1: Liberty Config aktualisieren

```bash
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs

# Trigger Config Reload
touch src/main/liberty/config/server.xml

# Warte auf Log-Meldung
# → "CWWKG0017I: The server configuration was successfully updated"
```

### Schritt 2: Frontend neu starten

```bash
# In IntelliJ:
# 1. Stoppe DashAppRunner
# 2. Starte DashAppRunner neu
# 3. Prüfe Logs:
#    ✅ Automatic login successful
#    ✅ Authorization header added
```

### Schritt 3: Ersten Request testen

**Erwartete Client-Logs:**
```
=== AuthorizationHeaderFilter called ===
  Request: GET http://localhost:9080/jeeeraaah/taskgroup/allFlat
  isLoggedIn(): true
  Token present: true
  Token length: 1386
  ✅ Authorization header added
```

**Erwartete Server-Logs:**
```
✅ KEIN "Unauthorized" Fehler mehr
✅ Request wird verarbeitet
✅ Response: 200 OK
```

---

## 🐛 HÄUFIGE PROBLEME

### Problem 1: Token wird nicht gesendet

**Symptom:**
```
⚠️ User is not logged in - no Authorization header added
```

**Lösung:**
1. DashAppRunner neu starten
2. Prüfe `testing.properties`
3. Prüfe Keycloak läuft und ist healthy

---

### Problem 2: Token wird gesendet, aber 403

**Symptom:**
```
✅ Authorization header added
(aber trotzdem 403 Forbidden)
```

**Lösung:**
1. Prüfe `groupNameAttribute` in `server.xml`
2. Liberty Config Reload (siehe oben)
3. Prüfe Keycloak Realm hat Rollen konfiguriert:
   ```bash
   ruu-keycloak-setup
   ```

---

### Problem 3: Liberty erkennt Config-Änderung nicht

**Symptom:**
```
CWWKG0018I: The server configuration was not updated. 
No functional changes were detected.
```

**Lösung - Liberty MUSS neu gestartet werden:**
```bash
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs

# Stoppe Liberty
pkill -f "liberty:dev"

# Warte 2 Sekunden
sleep 2

# Starte Liberty neu
mvn liberty:dev

# Warte auf: "CWWKF0011I: The defaultServer server is ready"
```

---

### Problem 4: Alte Config wird verwendet

**Symptom:**
```
grep "groupNameAttribute" ... → NICHTS gefunden
```

**Lösung:**
```bash
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs

# Lösche target, dann rebuild
mvn clean

# Warte bis liberty:dev neu baut
# Dann: touch src/main/liberty/config/server.xml
```

---

## 📋 QUICK-CHECK CHECKLISTE

- [ ] Keycloak Container läuft (healthy)
- [ ] Keycloak Realm `jeeeraaah-realm` existiert
- [ ] Liberty Server läuft (`liberty:dev`)
- [ ] `server.xml` hat `groupNameAttribute="realm_access/roles"`
- [ ] Liberty hat Config neu geladen (`CWWKG0017I`)
- [ ] DashAppRunner automatisches Login erfolgreich
- [ ] Authorization Header wird gesendet
- [ ] Erster Request: 200 OK (kein 403)

---

## 🔗 SIEHE AUCH

- [JWT-AUTHORIZATION-FIX.md](JWT-AUTHORIZATION-FIX.md) - Die vollständige Erklärung des Fixes
- [FRONTEND-SESSION-EXPIRED-FIX.md](FRONTEND-SESSION-EXPIRED-FIX.md) - Login-Probleme
- [STARTUP-QUICK-GUIDE.md](STARTUP-QUICK-GUIDE.md) - Vollständiger Startup-Ablauf
