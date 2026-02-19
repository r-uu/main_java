# Identity and Access Management (IAM) mit Keycloak und Open Liberty

**Umfassender Leitfaden für Authentifizierung und Autorisierung**

---

## 📋 Inhaltsverzeichnis

1. [Überblick](#überblick)
2. [Architektur](#architektur)
3. [Keycloak Server-Konfiguration](#keycloak-server-konfiguration)
4. [Open Liberty Server-Konfiguration](#open-liberty-server-konfiguration)
5. [Frontend/Client-Konfiguration](#frontendclient-konfiguration)
6. [Schritt-für-Schritt Anleitung](#schritt-für-schritt-anleitung)
7. [Troubleshooting](#troubleshooting)
8. [Best Practices](#best-practices)

---

## Überblick

### Was ist IAM?

**Identity and Access Management (IAM)** umfasst:
- **Authentifizierung** (Authentication): "Wer bist du?" - Identitätsnachweis
- **Autorisierung** (Authorization): "Was darfst du?" - Zugriffskontrolle
- **Benutzerverwaltung**: Verwaltung von Benutzern, Rollen und Rechten

### Technologie-Stack

```
┌─────────────────────────────────────────────────────────┐
│                    JavaFX Frontend                      │
│         (de.ruu.app.jeeeraaah.frontend.ui.fx)           │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP + JWT Bearer Token
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Open Liberty Server (Backend)              │
│         (de.ruu.app.jeeeraaah.backend.api.ws.rs)        │
│   ┌───────────────────────────────────────────────┐     │
│   │  MicroProfile JWT (mpJwt-2.1)                 │     │
│   │  - JWT Token Validierung                      │     │
│   │  - Signatur-Prüfung via JWKS                  │     │
│   │  - Rollen-Extraktion aus "groups" Claim       │     │
│   └───────────────────────────────────────────────┘     │
└────────────────────┬────────────────────────────────────┘
                     │ JWKS (Public Keys)
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Keycloak Server                        │
│   ┌───────────────────────────────────────────────┐     │
│   │  Realm: jeeeraaah-realm                       │     │
│   │  - User Management                            │     │
│   │  - Roles & Groups                             │     │
│   │  - OAuth2 / OpenID Connect                    │     │
│   │  - JWT Token Issuer                           │     │
│   └───────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

### OAuth2 / OpenID Connect Flow

```
Frontend                Keycloak                 Backend
   │                        │                        │
   │ 1. Login Request       │                        │
   │ ──────────────────────>│                        │
   │   (username/password)  │                        │
   │                        │                        │
   │ 2. JWT Access Token    │                        │
   │ <──────────────────────│                        │
   │                        │                        │
   │ 3. API Request         │                        │
   │    + Bearer Token      │                        │
   │ ──────────────────────────────────────────────> │
   │                        │                        │
   │                        │ 4. Token Validation    │
   │                        │ <──────────────────────│
   │                        │    (JWKS Public Keys)  │
   │                        │                        │
   │                        │ 5. Signature OK        │
   │                        │ ──────────────────────>│
   │                        │                        │
   │                        │ 6. Extract Roles       │
   │                        │    from "groups" claim │
   │                        │                        │
   │ 7. API Response        │                        │
   │ <───────────────────────────────────────────────│
   │                        │                        │
```

---

## Architektur

### Komponenten-Übersicht

#### 1. **Keycloak Server** (Port 8080)
- **Rolle**: Identity Provider (IdP) & Authorization Server
- **Aufgaben**:
  - Benutzerverwaltung (User Database)
  - Authentifizierung (Login)
  - JWT Token Generierung
  - Rollen- und Gruppenverwaltung
  - OAuth2 / OpenID Connect Provider

#### 2. **Open Liberty Server** (Port 9080)
- **Rolle**: Resource Server (Backend API)
- **Aufgaben**:
  - REST API bereitstellen
  - JWT Token validieren
  - Zugriffskontrolle auf Endpoints (@RolesAllowed)
  - Business Logic ausführen

#### 3. **JavaFX Frontend**
- **Rolle**: Client Application
- **Aufgaben**:
  - Benutzer-Login
  - Token-Verwaltung
  - Authentifizierte API-Aufrufe

---

## Keycloak Server-Konfiguration

### 1. Realm erstellen

Ein **Realm** ist eine isolierte Domäne für Benutzer und Anwendungen.

```java
// Automatische Realm-Erstellung via KeycloakRealmSetup.java
RealmRepresentation realm = new RealmRepresentation();
realm.setRealm("jeeeraaah-realm");
realm.setEnabled(true);
realm.setDisplayName("Jeeeraaah Task Management");
realm.setLoginTheme("keycloak");

keycloak.realms().create(realm);
```

**Manuelle Erstellung in der Admin Console:**

1. Login: http://localhost:8080/admin (admin/admin)
2. Klicke oben links auf "master" → "Create Realm"
3. Realm Name: `jeeeraaah-realm`
4. Enabled: ✅
5. Klicke "Create"

---

### 2. Client konfigurieren

Ein **Client** repräsentiert die Frontend-Anwendung.

#### Client Settings

```java
// Automatische Client-Erstellung
ClientRepresentation client = new ClientRepresentation();
client.setClientId("jeeeraaah-frontend");
client.setName("Jeeeraaah Frontend");
client.setEnabled(true);
client.setPublicClient(true);  // Kein Client Secret (Public Client)
client.setDirectAccessGrantsEnabled(true);  // Resource Owner Password Flow
client.setStandardFlowEnabled(true);  // Authorization Code Flow
client.setServiceAccountsEnabled(false);

// Redirect URIs
client.setRedirectUris(Arrays.asList(
    "http://localhost:*",
    "https://localhost:*"
));

// Web Origins (CORS)
client.setWebOrigins(Arrays.asList("*"));
```

**Manuelle Konfiguration:**

1. Wähle Realm: `jeeeraaah-realm`
2. Klicke "Clients" → "Create client"
3. **General Settings:**
   - Client type: `OpenID Connect`
   - Client ID: `jeeeraaah-frontend`
4. **Capability config:**
   - Client authentication: `Off` (Public Client)
   - Authorization: `Off`
   - Authentication flow:
     - ✅ Standard flow
     - ✅ Direct access grants
     - ❌ Implicit flow
     - ❌ Service accounts roles
5. **Login settings:**
   - Valid redirect URIs: `http://localhost:*`
   - Valid post logout redirect URIs: `http://localhost:*`
   - Web origins: `*`

---

### 3. Protocol Mappers konfigurieren

**Protocol Mappers** fügen zusätzliche Claims (Daten) zum JWT Token hinzu.

#### 3.1 Groups Claim Mapper (WICHTIG!)

Open Liberty erwartet Rollen im **Top-Level "groups" Claim**, nicht verschachtelt in `realm_access.roles`.

```java
// Automatische Mapper-Erstellung
ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
mapper.setName("groups-claim-mapper");
mapper.setProtocol("openid-connect");
mapper.setProtocolMapper("oidc-usermodel-realm-role-mapper");

Map<String, String> config = new HashMap<>();
config.put("claim.name", "groups");           // Liberty erwartet "groups"
config.put("jsonType.label", "String");
config.put("multivalued", "true");            // Array von Rollen
config.put("id.token.claim", "true");         // In ID Token
config.put("access.token.claim", "true");     // In Access Token
config.put("userinfo.token.claim", "true");   // In UserInfo Endpoint

mapper.setConfig(config);
```

**Manuelle Konfiguration:**

1. Client: `jeeeraaah-frontend` → "Client scopes" → "jeeeraaah-frontend-dedicated"
2. "Add mapper" → "By configuration"
3. Wähle: `User Realm Role`
4. **Konfiguration:**
   - Name: `groups-claim-mapper`
   - Token Claim Name: `groups`
   - Claim JSON Type: `String`
   - Multivalued: ✅
   - Add to ID token: ✅
   - Add to access token: ✅
   - Add to userinfo: ✅

**Vorher (Keycloak Standard):**
```json
{
  "realm_access": {
    "roles": ["admin", "user"]
  }
}
```

**Nachher (Liberty-kompatibel):**
```json
{
  "groups": ["admin", "user"]
}
```

#### 3.2 Audience Mapper (Optional)

Fügt die Audience `jeeeraaah-backend` zum Token hinzu.

```java
ProtocolMapperRepresentation audienceMapper = new ProtocolMapperRepresentation();
audienceMapper.setName("audience-mapper");
audienceMapper.setProtocol("openid-connect");
audienceMapper.setProtocolMapper("oidc-audience-mapper");

Map<String, String> config = new HashMap<>();
config.put("included.custom.audience", "jeeeraaah-backend");
config.put("access.token.claim", "true");
config.put("id.token.claim", "false");

audienceMapper.setConfig(config);
```

**Manuelle Konfiguration:**

1. Client: `jeeeraaah-frontend` → "Client scopes" → "jeeeraaah-frontend-dedicated"
2. "Add mapper" → "By configuration" → "Audience"
3. **Konfiguration:**
   - Name: `audience-mapper`
   - Included Custom Audience: `jeeeraaah-backend`
   - Add to access token: ✅
   - Add to ID token: ❌

---

### 4. Rollen erstellen

**Realm Roles** definieren die Berechtigungen.

```java
// Automatisch
RoleRepresentation adminRole = new RoleRepresentation();
adminRole.setName("admin");
adminRole.setDescription("Administrator role with full access");

RoleRepresentation userRole = new RoleRepresentation();
userRole.setName("user");
userRole.setDescription("Regular user role with limited access");

keycloak.realm("jeeeraaah-realm").roles().create(adminRole);
keycloak.realm("jeeeraaah-realm").roles().create(userRole);
```

**Manuelle Erstellung:**

1. Realm: `jeeeraaah-realm` → "Realm roles"
2. "Create role"
3. **Admin Role:**
   - Role name: `admin`
   - Description: `Administrator role with full access`
4. **User Role:**
   - Role name: `user`
   - Description: `Regular user role with limited access`

---

### 5. Benutzer erstellen

```java
// Automatisch
UserRepresentation user = new UserRepresentation();
user.setUsername("testuser");
user.setEmail("testuser@example.com");
user.setFirstName("Test");
user.setLastName("User");
user.setEnabled(true);
user.setEmailVerified(true);

// Benutzer erstellen
Response response = keycloak.realm("jeeeraaah-realm")
    .users().create(user);
String userId = CreatedResponseUtil.getCreatedId(response);

// Passwort setzen
CredentialRepresentation credential = new CredentialRepresentation();
credential.setType(CredentialRepresentation.PASSWORD);
credential.setValue("password123");
credential.setTemporary(false);

keycloak.realm("jeeeraaah-realm")
    .users().get(userId)
    .resetPassword(credential);

// Rollen zuweisen
RoleRepresentation adminRole = keycloak.realm("jeeeraaah-realm")
    .roles().get("admin").toRepresentation();

keycloak.realm("jeeeraaah-realm")
    .users().get(userId)
    .roles().realmLevel()
    .add(Arrays.asList(adminRole));
```

**Manuelle Erstellung:**

1. Realm: `jeeeraaah-realm` → "Users"
2. "Create new user"
3. **User Details:**
   - Username: `testuser`
   - Email: `testuser@example.com`
   - First name: `Test`
   - Last name: `User`
   - Email verified: ✅
   - Enabled: ✅
4. Klicke "Create"
5. **Credentials Tab:**
   - Set password: `password123`
   - Password temporary: ❌
6. **Role mapping Tab:**
   - "Assign role" → Wähle `admin` und/oder `user`

---

### 6. Keycloak Setup automatisieren

Das Projekt enthält eine automatische Setup-Klasse:

```bash
# Docker-Container laufen lassen
docker-compose up -d

# Setup ausführen (im Projekt)
cd root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

**Was macht KeycloakRealmSetup.java?**

1. ✅ Realm `jeeeraaah-realm` erstellen (falls nicht vorhanden)
2. ✅ Client `jeeeraaah-frontend` konfigurieren
3. ✅ Groups Claim Mapper hinzufügen
4. ✅ Audience Mapper hinzufügen
5. ✅ Rollen `admin` und `user` erstellen
6. ✅ Testbenutzer erstellen und Rollen zuweisen

**Ausgabe:**
```
✅ Realm 'jeeeraaah-realm' ist bereit
✅ Client 'jeeeraaah-frontend' konfiguriert
✅ 'groups' Claim Mapper erfolgreich erstellt
   → Rollen werden nun als Top-Level 'groups' Claim ins Token geschrieben
   → Liberty Server kann Rollen jetzt lesen!
✅ Audience Mapper erstellt
✅ Rolle 'admin' erstellt
✅ Rolle 'user' erstellt
✅ Benutzer 'testuser' erstellt
✅ Rolle 'admin' zu Benutzer 'testuser' zugewiesen
```

---

## Open Liberty Server-Konfiguration

### 1. Feature Manager

In `server.xml` müssen folgende Features aktiviert sein:

```xml
<featureManager>
    <!-- Jakarta EE 10.0 + MicroProfile 6.1 -->
    <feature>jakartaee-10.0</feature>
    <feature>microProfile-6.1</feature>
    
    <!-- MicroProfile JWT für Token-Validierung -->
    <feature>mpJwt-2.1</feature>
    
    <!-- Application Security für @RolesAllowed -->
    <feature>appSecurity-5.0</feature>
</featureManager>
```

---

### 2. MicroProfile JWT Konfiguration

```xml
<mpJwt id="jwtConsumer"
    jwksUri="http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs"
    issuer="http://localhost:8080/realms/jeeeraaah-realm"
    userNameAttribute="preferred_username"
    groupNameAttribute="groups">
    
    <!-- OPTIONAL: Audience Validierung (für Production) -->
    <!-- audiences="jeeeraaah-backend" -->
</mpJwt>
```

#### Erklärung der Attribute:

| Attribut | Beschreibung | Beispiel |
|----------|--------------|----------|
| `jwksUri` | URL zu Keycloaks Public Keys (JWKS = JSON Web Key Set)<br>Liberty lädt diese Keys und prüft die Token-Signatur | `http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs` |
| `issuer` | Erwarteter Token-Aussteller (Issuer Claim `iss`)<br>Muss exakt mit Keycloak Realm URL übereinstimmen | `http://localhost:8080/realms/jeeeraaah-realm` |
| `userNameAttribute` | JWT Claim für den Benutzernamen<br>Liberty extrahiert diesen Wert als Principal | `preferred_username` |
| `groupNameAttribute` | JWT Claim für Rollen/Gruppen<br>⚠️ **WICHTIG**: Muss auf `groups` gesetzt sein! | `groups` |
| `audiences` | Erwartete Audience (Optional)<br>Für Production-Sicherheit aktivieren | `jeeeraaah-backend` |

#### Wichtige URLs:

| URL | Beschreibung |
|-----|--------------|
| `http://localhost:8080/realms/jeeeraaah-realm/.well-known/openid-configuration` | OpenID Connect Discovery (Metadaten) |
| `http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs` | JWKS Public Keys (Token-Signatur-Validierung) |
| `http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token` | Token Endpoint (Login) |

---

### 3. REST Endpoint absichern

```java
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/taskgroup")
public class TaskGroupResource
{
    /**
     * Öffentlicher Endpoint - kein Login erforderlich
     */
    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getPublicData()
    {
        return Response.ok("Public data").build();
    }
    
    /**
     * Geschützter Endpoint - nur für angemeldete Benutzer
     * (unabhängig von der Rolle)
     */
    @GET
    @Path("/protected")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response getProtectedData(@Context SecurityContext securityContext)
    {
        String username = securityContext.getUserPrincipal().getName();
        return Response.ok("Hello " + username).build();
    }
    
    /**
     * Admin-Endpoint - nur für Admin-Rolle
     */
    @GET
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response getAdminData(@Context SecurityContext securityContext)
    {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("admin");
        
        return Response.ok("Admin access granted for " + username).build();
    }
}
```

#### Annotations Übersicht:

| Annotation | Beschreibung |
|------------|--------------|
| `@PermitAll` | Jeder darf zugreifen (kein Token erforderlich) |
| `@DenyAll` | Niemand darf zugreifen |
| `@RolesAllowed("admin")` | Nur Benutzer mit Rolle `admin` |
| `@RolesAllowed({"user", "admin"})` | Benutzer mit Rolle `user` ODER `admin` |

---

### 4. SecurityContext nutzen

```java
@Context
private SecurityContext securityContext;

public void someMethod()
{
    // Benutzername abrufen
    String username = securityContext.getUserPrincipal().getName();
    
    // Rolle prüfen
    boolean isAdmin = securityContext.isUserInRole("admin");
    boolean isUser = securityContext.isUserInRole("user");
    
    // JWT Token im Request Header verfügbar via:
    // @HeaderParam("Authorization") String authHeader
}
```

---

## Frontend/Client-Konfiguration

### 1. Login-Flow im JavaFX Frontend

```java
@Singleton
@Slf4j
public class KeycloakAuthService
{
    private String keycloakServerUrl;
    private String realm;
    private String clientId;
    private String tokenUrl;
    
    @Getter private String accessToken;
    @Getter private String refreshToken;
    
    @PostConstruct
    private void init()
    {
        // Konfiguration aus microprofile-config.properties
        keycloakServerUrl = ConfigProvider.getConfig()
            .getOptionalValue("keycloak.auth.server.url", String.class)
            .orElse("http://localhost:8080");
        
        realm = ConfigProvider.getConfig()
            .getOptionalValue("keycloak.realm", String.class)
            .orElse("jeeeraaah-realm");
        
        clientId = ConfigProvider.getConfig()
            .getOptionalValue("keycloak.client-id", String.class)
            .orElse("jeeeraaah-frontend");
        
        tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
            keycloakServerUrl, realm);
    }
    
    /**
     * Benutzer-Login mit Resource Owner Password Grant
     */
    public String login(String username, String password) 
        throws IOException, InterruptedException
    {
        String formData = String.format(
            "grant_type=password&client_id=%s&username=%s&password=%s",
            URLEncoder.encode(clientId, StandardCharsets.UTF_8),
            URLEncoder.encode(username, StandardCharsets.UTF_8),
            URLEncoder.encode(password, StandardCharsets.UTF_8)
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200)
        {
            JsonNode json = new ObjectMapper().readTree(response.body());
            accessToken = json.get("access_token").asText();
            refreshToken = json.get("refresh_token").asText();
            
            log.info("Login erfolgreich für Benutzer: {}", username);
            return accessToken;
        }
        else
        {
            log.error("Login fehlgeschlagen: {}", response.body());
            throw new IOException("Login failed: " + response.statusCode());
        }
    }
}
```

---

### 2. Authentifizierte API-Aufrufe

```java
@ApplicationScoped
public class TaskGroupServiceClient
{
    @Inject
    private KeycloakAuthService authService;
    
    private final Client client = ClientBuilder.newClient();
    
    public TaskGroupDTO getTaskGroup(Long id)
    {
        String token = authService.getAccessToken();
        
        Response response = client
            .target("http://localhost:9080/taskgroup/{id}")
            .resolveTemplate("id", id)
            .request(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)  // ← JWT Token!
            .get();
        
        if (response.getStatus() == 200)
        {
            return response.readEntity(TaskGroupDTO.class);
        }
        else if (response.getStatus() == 401)
        {
            throw new RuntimeException("Unauthorized - Token ungültig");
        }
        else if (response.getStatus() == 403)
        {
            throw new RuntimeException("Forbidden - Keine Berechtigung");
        }
        else
        {
            throw new RuntimeException("API Error: " + response.getStatus());
        }
    }
}
```

---

### 3. Authorization Header Filter (Global)

Automatisch den JWT Token zu allen Requests hinzufügen:

```java
@Provider
public class AuthorizationHeaderFilter implements ClientRequestFilter
{
    @Inject
    private KeycloakAuthService authService;
    
    @Override
    public void filter(ClientRequestContext requestContext)
    {
        String token = authService.getAccessToken();
        
        if (token != null && !token.isEmpty())
        {
            requestContext.getHeaders()
                .putSingle("Authorization", "Bearer " + token);
        }
    }
}

// Filter registrieren
Client client = ClientBuilder.newClient()
    .register(AuthorizationHeaderFilter.class);
```

---

## Schritt-für-Schritt Anleitung

### Setup von Grund auf

#### Schritt 1: Docker-Container starten

```bash
cd /home/r-uu/develop/github/main
docker-compose up -d

# Status prüfen
docker ps | grep -E "keycloak|postgres"
```

**Erwartete Ausgabe:**
```
keycloak   Up 2 minutes (healthy)
postgres   Up 2 minutes (healthy)
```

---

#### Schritt 2: Keycloak automatisch konfigurieren

```bash
cd root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

**Erwartete Ausgabe:**
```
✅ Realm 'jeeeraaah-realm' ist bereit
✅ Client 'jeeeraaah-frontend' konfiguriert
✅ 'groups' Claim Mapper erfolgreich erstellt
✅ Audience Mapper erstellt
✅ Rollen erstellt: admin, user
✅ Testbenutzer 'testuser' erstellt
```

---

#### Schritt 3: Keycloak Admin Console öffnen (Optional - zur Verifikation)

**URL:** http://localhost:8080/admin

**Login:**
- Username: `admin`
- Password: `admin`

**Prüfen:**
1. Realm: `jeeeraaah-realm` existiert
2. Client: `jeeeraaah-frontend` existiert
3. Users: `testuser` existiert mit Rolle `admin`
4. Client Scopes: `jeeeraaah-frontend-dedicated` → Mappers:
   - ✅ `groups-claim-mapper`
   - ✅ `audience-mapper`

---

#### Schritt 4: Open Liberty Server starten

```bash
cd root/app/jeeeraaah/backend/api/ws.rs

# Mit Maven
mvn liberty:dev

# Oder direkt
mvn liberty:run
```

**Erwartete Ausgabe:**
```
[AUDIT   ] CWWKZ0001I: Application jeeeraaah started in 5.123 seconds.
[AUDIT   ] CWWKF0012I: The server installed the following features: 
           [jakartaee-10.0, mpJwt-2.1, appSecurity-5.0, ...]
```

**Server läuft auf:** http://localhost:9080

---

#### Schritt 5: JavaFX Frontend starten

```bash
cd root/app/jeeeraaah/frontend/ui/fx

# App starten
mvn javafx:run

# Oder den AppRunner
java -jar target/r-uu.app.jeeeraaah.frontend.ui.fx-0.0.1.jar
```

---

#### Schritt 6: Login testen

1. Frontend öffnet sich
2. Login-Dialog erscheint
3. Eingeben:
   - **Username:** `testuser`
   - **Password:** `password123`
4. Klicke "Login"

**Erwartetes Ergebnis:**
```
✅ Login erfolgreich
✅ Access Token erhalten
✅ Dashboard wird angezeigt
```

---

#### Schritt 7: API-Aufruf testen (manuell)

```bash
# 1. Token abrufen
TOKEN=$(curl -s -X POST \
  "http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=jeeeraaah-frontend" \
  -d "username=testuser" \
  -d "password=password123" \
  | jq -r '.access_token')

# 2. API-Aufruf mit Token
curl -X GET \
  "http://localhost:9080/taskgroup/allFlat" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

**Erwartete Antwort:**
```json
[
  {
    "id": 1,
    "name": "Feature Set 1",
    "description": "Backend Development"
  }
]
```

---

## Troubleshooting

### Problem 1: "Unauthorized 401" bei API-Aufrufen

**Symptom:**
```
HTTP 401 Unauthorized
```

**Mögliche Ursachen:**

#### 1.1 Token fehlt oder ist ungültig

```bash
# Token dekodieren (mit jwt.io oder jq)
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq
```

**Prüfen:**
- `exp` (Expiration): Token noch gültig?
- `iss` (Issuer): Stimmt mit Liberty `issuer` überein?
- `groups`: Enthält die erforderlichen Rollen?

**Lösung:**
```bash
# Neuen Token abrufen
TOKEN=$(curl -s -X POST ...)
```

#### 1.2 Liberty kann JWKS nicht erreichen

**Liberty Logs prüfen:**
```bash
tail -f wlp/usr/servers/defaultServer/logs/messages.log
```

**Fehler:**
```
CWWKS6031E: The JSON Web Token (JWT) consumer cannot contact the URL 
[http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs]
```

**Lösung:**
- Keycloak läuft: `docker ps | grep keycloak`
- JWKS erreichbar: `curl http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs`

#### 1.3 Issuer stimmt nicht überein

**Liberty Logs:**
```
CWWKS6022E: The issuer [http://localhost:8080/realms/jeeeraaah-realm] 
specified in the token does not match the issuer attribute
```

**Lösung:**
`server.xml` prüfen:
```xml
<mpJwt issuer="http://localhost:8080/realms/jeeeraaah-realm" />
```

---

### Problem 2: "Forbidden 403" bei API-Aufrufen

**Symptom:**
```
HTTP 403 Forbidden
```

**Ursache:** Token ist gültig, aber Benutzer hat nicht die erforderliche Rolle.

**Diagnose:**

```bash
# Token dekodieren
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq '.groups'
```

**Erwartete Ausgabe:**
```json
["admin", "user"]
```

**Wenn `groups` fehlt:**

→ **Groups Claim Mapper fehlt!**

**Lösung:**

1. Keycloak Admin Console öffnen
2. Client: `jeeeraaah-frontend` → Client scopes → `jeeeraaah-frontend-dedicated`
3. Mappers prüfen: `groups-claim-mapper` muss existieren
4. Falls nicht: KeycloakRealmSetup erneut ausführen

---

### Problem 3: "groups" Claim fehlt im Token

**Symptom:**
```json
{
  "realm_access": {
    "roles": ["admin"]
  }
}
```

**Aber Liberty erwartet:**
```json
{
  "groups": ["admin"]
}
```

**Ursache:** Groups Claim Mapper fehlt oder falsch konfiguriert.

**Lösung:**

```java
// KeycloakRealmSetup erneut ausführen
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

**Oder manuell:**

1. Keycloak Admin Console
2. Client: `jeeeraaah-frontend` → Client scopes → `jeeeraaah-frontend-dedicated`
3. "Add mapper" → "By configuration" → "User Realm Role"
4. **Konfiguration:**
   - Name: `groups-claim-mapper`
   - Token Claim Name: `groups`  ← **WICHTIG!**
   - Claim JSON Type: `String`
   - Multivalued: ✅
   - Add to access token: ✅

---

### Problem 4: Keycloak-Container startet nicht

**Symptom:**
```bash
docker ps | grep keycloak
# Kein Output oder Status: unhealthy
```

**Diagnose:**
```bash
docker logs keycloak
```

**Mögliche Ursachen:**

#### 4.1 PostgreSQL nicht bereit

**Fehler:**
```
Caused by: org.postgresql.util.PSQLException: Connection refused
```

**Lösung:**
```bash
# PostgreSQL-Status prüfen
docker ps | grep postgres

# Falls nicht healthy: Neustarten
docker-compose restart postgres

# Warten bis healthy, dann Keycloak starten
docker-compose up -d keycloak
```

#### 4.2 Port 8080 bereits belegt

**Fehler:**
```
Error starting userland proxy: listen tcp4 0.0.0.0:8080: bind: address already in use
```

**Lösung:**
```bash
# Port 8080 freimachen oder in docker-compose.yml ändern
lsof -i :8080
kill <PID>
```

---

### Problem 5: Liberty findet keine Rollen

**Symptom:**
- Login erfolgreich
- API-Aufruf liefert 403 Forbidden
- `SecurityContext.isUserInRole("admin")` liefert `false`

**Diagnose:**

**Liberty server.xml prüfen:**
```xml
<mpJwt groupNameAttribute="groups" />
```

**NICHT:**
```xml
<mpJwt groupNameAttribute="realm_access.roles" />
```

**Token prüfen:**
```bash
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq '.groups'
```

**Erwartete Ausgabe:**
```json
["admin", "user"]
```

**Wenn `null`:** → Groups Claim Mapper fehlt (siehe Problem 3)

---

## Best Practices

### Security

#### 1. **Production: Audience validieren**

**Development:**
```xml
<mpJwt id="jwtConsumer"
    jwksUri="..."
    issuer="..."
    userNameAttribute="preferred_username"
    groupNameAttribute="groups">
    <!-- audiences deaktiviert -->
</mpJwt>
```

**Production:**
```xml
<mpJwt id="jwtConsumer"
    jwksUri="..."
    issuer="..."
    audiences="jeeeraaah-backend"  ← Aktivieren!
    userNameAttribute="preferred_username"
    groupNameAttribute="groups" />
```

#### 2. **HTTPS verwenden**

**Production:**
```xml
<!-- Keycloak -->
https://auth.example.com/realms/jeeeraaah-realm/...

<!-- Liberty -->
<httpEndpoint httpsPort="9443" httpPort="-1" />
```

#### 3. **Client Secret für vertrauliche Clients**

Für **Backend-zu-Backend** Kommunikation:

```java
// Keycloak Client-Konfiguration
client.setPublicClient(false);  // Vertraulicher Client
client.setClientAuthenticatorType("client-secret");

// Token-Anfrage mit Client Secret
curl -X POST "http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token" \
  -d "grant_type=client_credentials" \
  -d "client_id=jeeeraaah-backend-service" \
  -d "client_secret=YOUR_SECRET_HERE"
```

#### 4. **Token Expiration konfigurieren**

**Keycloak Admin Console:**
1. Realm: `jeeeraaah-realm` → Realm settings → Tokens
2. **Access Token Lifespan:** 5 Minuten (Standard)
3. **Refresh Token Lifespan:** 30 Minuten
4. **SSO Session Max:** 10 Stunden

#### 5. **Refresh Tokens nutzen**

```java
public String refreshAccessToken() throws IOException, InterruptedException
{
    String formData = String.format(
        "grant_type=refresh_token&client_id=%s&refresh_token=%s",
        URLEncoder.encode(clientId, StandardCharsets.UTF_8),
        URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
    );
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(tokenUrl))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(formData))
        .build();
    
    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    if (response.statusCode() == 200)
    {
        JsonNode json = new ObjectMapper().readTree(response.body());
        accessToken = json.get("access_token").asText();
        refreshToken = json.get("refresh_token").asText();
        return accessToken;
    }
    else
    {
        throw new IOException("Token refresh failed");
    }
}
```

---

### Performance

#### 1. **JWKS Caching**

Liberty cached die Public Keys automatisch. Konfigurierbar via:

```xml
<mpJwt jwksCacheLifetimeMs="600000" />  <!-- 10 Minuten -->
```

#### 2. **Connection Pooling**

```java
// Jersey Client mit Connection Pooling
ClientConfig config = new ClientConfig();
config.property(ClientProperties.CONNECT_TIMEOUT, 5000);
config.property(ClientProperties.READ_TIMEOUT, 30000);

Client client = ClientBuilder.newClient(config);
```

---

### Monitoring

#### 1. **Liberty Logs überwachen**

```bash
tail -f wlp/usr/servers/defaultServer/logs/messages.log | grep -E "CWWKS|JWT"
```

#### 2. **Keycloak Events aktivieren**

**Admin Console:**
1. Realm: `jeeeraaah-realm` → Events
2. **User events settings:**
   - Save events: ✅
   - Event listeners: `jboss-logging`
3. **Login events settings:**
   - Save login events: ✅

**Events anzeigen:**
- Login Events Tab: Zeigt erfolgreiche/fehlgeschlagene Logins
- User Events Tab: Zeigt Token-Generierungen

---

## Zusammenfassung

### Wichtigste Konfigurationen

| Komponente | Konfiguration | Wert |
|------------|---------------|------|
| **Keycloak** | Realm | `jeeeraaah-realm` |
| | Client ID | `jeeeraaah-frontend` |
| | Client Type | Public Client |
| | **Groups Claim Mapper** | **Claim Name: `groups`** ← KRITISCH! |
| | Token Endpoint | `http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token` |
| **Liberty** | Feature | `mpJwt-2.1`, `appSecurity-5.0` |
| | JWKS URI | `http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/certs` |
| | Issuer | `http://localhost:8080/realms/jeeeraaah-realm` |
| | **groupNameAttribute** | **`groups`** ← KRITISCH! |
| **Frontend** | Auth Service | `KeycloakAuthService.java` |
| | Token Header | `Authorization: Bearer <token>` |

### Checkliste für Setup

- [ ] Keycloak Container läuft (`docker ps`)
- [ ] PostgreSQL Container läuft und healthy
- [ ] Realm `jeeeraaah-realm` existiert
- [ ] Client `jeeeraaah-frontend` konfiguriert
- [ ] **Groups Claim Mapper** existiert mit `claim.name=groups`
- [ ] Rollen `admin` und `user` existieren
- [ ] Testbenutzer existiert mit Rollen
- [ ] Liberty `server.xml` konfiguriert:
  - [ ] `mpJwt` Feature aktiviert
  - [ ] `jwksUri` korrekt
  - [ ] `issuer` korrekt
  - [ ] **`groupNameAttribute="groups"`** gesetzt
- [ ] REST Endpoints mit `@RolesAllowed` gesichert
- [ ] Frontend kann sich anmelden
- [ ] API-Aufrufe mit Token funktionieren

---

**Erstellt:** 2026-02-16  
**Autor:** GitHub Copilot  
**Version:** 1.0  
**Status:** ✅ Produktionsreif

