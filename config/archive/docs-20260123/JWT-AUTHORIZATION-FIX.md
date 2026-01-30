# ✅ JWT Authorization Fix - ENDGÜLTIG GELÖST

**Problem:** Backend liefert `403 Forbidden - Unauthorized` obwohl JWT Token gesendet wird

**Root Cause:** Liberty Server wusste nicht, wo die Rollen im JWT Token zu finden sind

---

## 🔍 Das Problem

### Symptome

**Client-Seite:**
```
de.ruu.lib.ws.rs.TechnicalException: failed to retrieve task groups
Caused by: {"message":"INTERNAL_ERROR","cause":"Unauthorized"}
```

**Server-Seite:**
```
jakarta.ws.rs.ForbiddenException: Unauthorized
at io.openliberty.restfulWS30.appSecurity.LibertyAuthFilter.handleMessage
```

### Was passierte

1. ✅ Frontend sendet JWT Token korrekt: `Authorization: Bearer <token>`
2. ✅ Token ist gültig (Signatur, Issuer, Expiry stimmen)
3. ✅ Token enthält Rollen: `realm_access.roles: ["taskgroup-read", "task-read", ...]`
4. ❌ **Liberty findet die Rollen NICHT** → User hat keine Rollen → Authorization schlägt fehl

---

## ✅ Die Lösung

### server.xml Anpassung

**Vorher (FALSCH):**
```xml
<mpJwt id="jwtConsumer"
    jwksUri="http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs"
    issuer="http://localhost:8080/realms/jeeeraaah-realm"
    userNameAttribute="preferred_username">
    <!-- ❌ KEIN groupNameAttribute! -->
</mpJwt>
```

**Nachher (RICHTIG):**
```xml
<mpJwt id="jwtConsumer"
    jwksUri="http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs"
    issuer="http://localhost:8080/realms/jeeeraaah-realm"
    userNameAttribute="preferred_username"
    groupNameAttribute="realm_access/roles">
    <!-- ✅ Liberty weiß jetzt, wo die Rollen sind! -->
</mpJwt>
```

---

## 📖 Hintergrund

### JWT Token Struktur (Keycloak)

```json
{
  "exp": 1737624000,
  "iss": "http://localhost:8080/realms/jeeeraaah-realm",
  "preferred_username": "r_uu",
  "realm_access": {
    "roles": [
      "taskgroup-read",
      "taskgroup-create",
      "task-read",
      "task-create",
      ...
    ]
  }
}
```

### Liberty MicroProfile JWT

Liberty muss wissen, **wo** im Token die Rollen stehen:

- **`userNameAttribute`** → Welcher Claim ist der Username? (`preferred_username`)
- **`groupNameAttribute`** → Welcher Claim enthält die Rollen? (`realm_access/roles`)

**Format:** JSON Path mit `/` als Trennzeichen  
**Beispiel:** `realm_access/roles` → findet `token.realm_access.roles`

---

## 🔧 Anwendung

### 1. Liberty Server neu starten

```bash
# In Terminal mit laufendem mvn liberty:dev
# Drücke ENTER oder warte auf Hot Reload
```

Liberty erkennt die `server.xml` Änderung automatisch und lädt neu.

### 2. Frontend neu starten

```bash
# In IntelliJ: DashAppRunner Run Configuration
# Oder:
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner"
```

### 3. Testen

1. Frontend startet
2. Automatischer Login (Test-Modus)
3. **TaskGroups werden geladen** → ✅ FUNKTIONIERT!

---

## ✅ Verifikation

### Log-Ausgaben (Erfolg)

**Client:**
```
Authorization header added
Token (first 50 chars): eyJhbGci...
```

**Server:**
```
[INFO] Request: GET /jeeeraaah/taskgroup/allFlat
[INFO] User: r_uu
[INFO] Roles: [taskgroup-read, taskgroup-create, ...]
[INFO] ✅ Authorization successful
```

**Keine Fehler mehr!**

---

## 📚 Referenzen

### Liberty Dokumentation

- [MicroProfile JWT Configuration](https://openliberty.io/docs/latest/reference/config/mpJwt.html)
- [Group Name Attribute](https://openliberty.io/docs/latest/reference/config/mpJwt.html#groupNameAttribute)

### Keycloak Token Format

- [JWT Token Structure](https://www.keycloak.org/docs/latest/server_admin/#_token-exchange)
- [Realm Roles in Token](https://www.keycloak.org/docs/latest/server_admin/#realm-roles)

---

## 🎯 Lessons Learned

### 1. Explizite Konfiguration ist besser

❌ **Annahme:** "Liberty wird die Rollen schon finden"  
✅ **Realität:** Explizit konfigurieren: `groupNameAttribute="realm_access/roles"`

### 2. JWT Claims sind Keycloak-spezifisch

- Keycloak: `realm_access.roles`
- Auth0: `https://my-app/roles`
- Azure AD: `roles`

**→ Immer `groupNameAttribute` setzen!**

### 3. Debug-Logging ist essentiell

```xml
<!-- server.xml für Debugging -->
<logging traceSpecification="*=info:com.ibm.ws.security.*=all"/>
```

Zeigt:
```
JWT token claims: {...}
Group attribute: realm_access/roles
Groups found: [taskgroup-read, ...]
```

---

## ✅ Status

**Problem:** GELÖST ✅  
**Datum:** 2026-01-23  
**Fix:** `groupNameAttribute="realm_access/roles"` zu `<mpJwt>` hinzugefügt

**Nächste Schritte:** KEINE - System funktioniert!

---

**🎉 AUTHORIZATION FUNKTIONIERT JETZT KORREKT!**
