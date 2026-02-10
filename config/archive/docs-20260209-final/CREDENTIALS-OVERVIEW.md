# Credentials Übersicht

## 🔑 Credential-Strategie

**WICHTIG**: 
- Template-Dateien (`.env.template`) → Git-committed mit Platzhaltern
- Lokale Dateien (`.env`) → Git-ignored mit echten Werten

## 📦 Container Credentials

### 1. PostgreSQL (postgres-jeeeraaah)
**Zweck**: Hauptdatenbank für Jeeeraaah-App

| Variable | Template-Wert | Lokaler Wert | Verwendung |
|----------|---------------|--------------|------------|
| `POSTGRES_USER` | `YOUR_DB_USER` | `r_uu` | DB Admin User |
| `POSTGRES_PASSWORD` | `YOUR_DB_PASSWORD` | `r_uu_password` | DB Admin Password |
| `POSTGRES_DB` | `jeeeraaah` | `jeeeraaah` | Hauptdatenbank |

**Init-DB**: Erstellt automatisch auch `lib_test` Datenbank

### 2. PostgreSQL (postgres-keycloak)
**Zweck**: Keycloak Backend-Datenbank

| Variable | Template-Wert | Lokaler Wert | Verwendung |
|----------|---------------|--------------|------------|
| `POSTGRES_USER` | `YOUR_KEYCLOAK_DB_USER` | `keycloak` | Keycloak DB User |
| `POSTGRES_PASSWORD` | `YOUR_KEYCLOAK_DB_PASSWORD` | `keycloak_password` | Keycloak DB Password |
| `POSTGRES_DB` | `keycloak` | `keycloak` | Keycloak Datenbank |

### 3. Keycloak
**Zweck**: Identity & Access Management Server

| Variable | Template-Wert | Lokaler Wert | Verwendung |
|----------|---------------|--------------|------------|
| `KEYCLOAK_ADMIN` | `YOUR_KEYCLOAK_ADMIN` | `admin` | Keycloak Admin User |
| `KEYCLOAK_ADMIN_PASSWORD` | `YOUR_KEYCLOAK_ADMIN_PASSWORD` | `admin` | Keycloak Admin Password |
| `KC_DB_URL_HOST` | `postgres-keycloak` | `postgres-keycloak` | DB Host |
| `KC_DB_URL_PORT` | `5432` | `5432` | DB Port |
| `KC_DB_URL_DATABASE` | `keycloak` | `keycloak` | DB Name |
| `KC_DB_USERNAME` | `YOUR_KEYCLOAK_DB_USER` | `keycloak` | DB User (von postgres-keycloak) |
| `KC_DB_PASSWORD` | `YOUR_KEYCLOAK_DB_PASSWORD` | `keycloak_password` | DB Password (von postgres-keycloak) |

### 4. JasperReports Service
**Zweck**: Report Generation Service

Keine Credentials erforderlich (läuft ohne Authentifizierung)

## 🎯 Jeeeraaah Application Credentials

### Backend (Liberty Server)
Verwendet Keycloak für Authentication:
- **Keycloak Realm**: `jeeeraaah-realm`
- **Client ID**: `jeeeraaah-frontend`
- **Test User**: `r_uu` / `r_uu_password`

### Frontend (JavaFX Client)
Verbindet sich zu:
- **Backend**: `http://localhost:9080/jeeeraaah`
- **Keycloak**: `http://localhost:8080`

## 📁 Datei-Struktur

```
config/shared/docker/
├── .env.template          # Git-committed, Platzhalter
├── .env                   # Git-ignored, echte Werte
└── docker-compose.yml     # Verwendet .env Werte
```

## 🔒 Sicherheits-Hinweise

1. **.env ist in .gitignore** → NIE committen!
2. **.env.template committen** → Dokumentiert erforderliche Variablen
3. **Produktions-Werte** → Andere Werte als Template verwenden
4. **Passwort-Policy** → Mindestens 12 Zeichen, Mix aus Groß/Klein/Zahlen/Sonderzeichen
