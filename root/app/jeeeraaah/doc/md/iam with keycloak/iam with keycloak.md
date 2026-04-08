# Identity and Access Management mit Keycloak

Der **jeeeraaah** keycloak server läuft in einem Docker Container, der über `docker-compose` orchestriert wird. Damit das Identity and Access Management (IAM) mit Keycloak funktioniert, müssen folgende Schritte durchgeführt werden:

## Konfiguration von Keycloak

Die keycloak service Konfiguration erfolgt in docker-compose.yml. Dort wird der keycloak server mit den notwendigen Umgebungsvariablen konfiguriert, um die initiale Einrichtung von Realm, Client und User zu ermöglichen.

Das openliberty backend ist so konfiguriert, dass es bei eingehenden requests mit keycloak über OpenID Connect (OIDC) kommuniziert, um die Authentifizierung und Autorisierung der Benutzer zu gewährleisten. Das frontend kommuniziert direkt mit Keycloak, um die Authentifizierung und Autorisierung der Benutzer zu gewährleisten.