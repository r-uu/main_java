# jeeeraaah Frontend

Dieses Modul stellt das Frontend für die jeeeraaah-Anwendung bereit.

## Module

### api.client/ws_rs
REST Client für Zugriff auf das Backend:
- TaskGroupServiceClient
- TaskServiceClient  
- KeycloakAuthService
- AuthorizationHeaderFilter (automatische Token-Injection)

### ui/fx
JavaFX Desktop UI:
- DashApp - Hauptanwendung mit Dashboard
- LoginDialog - Keycloak-Authentifizierung
- Task-Management-Views
- TaskGroup-Management-Views

### common
Gemeinsame Frontend-Klassen und Utilities

## Starten

```bash
# DashApp (Hauptanwendung)
mvn exec:java -pl app/jeeeraaah/frontend/ui/fx \
  -Dexec.mainClass=de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
```

Siehe [Runner Apps Guide](../../../docs/entwicklung/RUNNER-APPS-GUIDE.md) für weitere Konfigurationen.

## Authentifizierung

Das Frontend nutzt Keycloak für Authentifizierung:
- Login-Dialog beim Start
- JWT Token-basierte API-Calls
- Automatisches Token-Refresh

Siehe [Keycloak JavaFX Integration](../../../docs/infrastruktur/keycloak/04-javafx-integration.md)
