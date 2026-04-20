# Configuration Guide - Single Source of Truth

## Overview

This project uses **environment variables** as the single source of truth for all configuration.

```
.env file (local, git-ignored)
    ↓
Environment Variables (UPPERCASE)
    ↓
MicroProfile Config (reads at runtime)
    ↓
Java Application
```

## No Maven Resource Filtering

**Important:** We do NOT use Maven resource filtering anymore. Properties files are copied as-is during build.

Configuration values are read **at runtime** from environment variables, not replaced during build time.

## Environment Variable Naming Convention

All environment variables use **UPPERCASE** names:

```properties
# ✅ Correct
POSTGRES_JEEERAAAH_DATABASE=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah

# ❌ Wrong (will not work)
postgres_jeeeraaah_database=jeeeraaah
postgres_jeeeraaah_user=jeeeraaah
```

## Configuration Files

### 1. `.env` (Single Source of Truth)

Location: `config/shared/docker/.env`

- Contains all configuration values
- Used by Docker Compose AND Java applications
- Git-ignored (never committed)
- Uses LF line endings (not CRLF)

Example:
```properties
POSTGRES_JEEERAAAH_HOST=localhost
POSTGRES_JEEERAAAH_PORT=5432
POSTGRES_JEEERAAAH_DATABASE=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=secret123
```

### 2. `.env.template`

Location: `config/shared/docker/.env.template`

- Template for creating `.env`
- Committed to git
- Contains placeholder passwords

### 3. MicroProfile Config Properties

Location: `**/META-INF/microprofile-config.properties`

- Define fallback values
- Reference environment variables with `${VAR_NAME:fallback}`
- Never contain actual secrets

Example:
```properties
database.host=${POSTGRES_JEEERAAAH_HOST:localhost}
database.port=${POSTGRES_JEEERAAAH_PORT:5432}
database.name=${POSTGRES_JEEERAAAH_DATABASE:jeeeraaah}
database.user=${POSTGRES_JEEERAAAH_USER:jeeeraaah}
database.password=${POSTGRES_JEEERAAAH_PASSWORD:jeeeraaah}
```

## How to Set Up

### 1. Create Local `.env` File

```bash
cd ~/develop/github/java/main/config/shared/docker
cp .env.template .env
# Edit .env and set real passwords
```

### 2. Docker Environment

Docker Compose automatically reads `.env` and sets environment variables for containers.

```bash
docker compose up -d
```

### 3. Local Development (IntelliJ)

Option A: Set environment variables in Run Configuration
- Run → Edit Configurations
- Add environment variables manually

Option B: Export in shell before starting IntelliJ
```bash
export POSTGRES_JEEERAAAH_HOST=localhost
export POSTGRES_JEEERAAAH_PORT=5432
# ... etc
```

## Environment Variables Reference

### PostgreSQL for jeeeraaah Application

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_JEEERAAAH_HOST` | `localhost` | PostgreSQL host |
| `POSTGRES_JEEERAAAH_PORT` | `5432` | PostgreSQL port |
| `POSTGRES_JEEERAAAH_DATABASE` | `jeeeraaah` | Database name |
| `POSTGRES_JEEERAAAH_USER` | `jeeeraaah` | Database user |
| `POSTGRES_JEEERAAAH_PASSWORD` | `jeeeraaah` | Database password |

### PostgreSQL for lib_test (Integration Tests)

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_LIB_TEST_DATABASE` | `lib_test` | Test database name |
| `POSTGRES_LIB_TEST_USER` | `lib_test` | Test database user |
| `POSTGRES_LIB_TEST_PASSWORD` | `lib_test` | Test database password |

Note: lib_test uses the same PostgreSQL container as jeeeraaah (host/port).

### PostgreSQL for Keycloak

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_KEYCLOAK_HOST` | `localhost` | Keycloak PostgreSQL host |
| `POSTGRES_KEYCLOAK_PORT` | `5433` | Keycloak PostgreSQL port |
| `POSTGRES_KEYCLOAK_DATABASE` | `keycloak` | Keycloak database name |
| `POSTGRES_KEYCLOAK_USER` | `keycloak` | Keycloak database user |
| `POSTGRES_KEYCLOAK_PASSWORD` | `keycloak` | Keycloak database password |

### Keycloak

| Variable | Default | Description |
|----------|---------|-------------|
| `KEYCLOAK_ADMIN_USER` | `admin` | Keycloak admin username |
| `KEYCLOAK_ADMIN_PASSWORD` | `admin` | Keycloak admin password |
| `KEYCLOAK_REALM` | `jeeeraaah-realm` | Keycloak realm name |

## Troubleshooting

### Problem: "password authentication failed for user postgres_jeeeraaah_username"

**Cause:** MicroProfile Config didn't find the environment variable, so it used the fallback value literally.

**Solution:**
1. Check `.env` file exists: `ls -la ~/develop/github/java/main/config/shared/docker/.env`
2. Check variable names are UPPERCASE
3. Restart application after changing `.env`
4. In Docker: `docker compose down && docker compose up -d`

### Problem: Maven tries to replace ${variables}

**Cause:** Maven resource filtering is enabled.

**Solution:** 
- Check `pom.xml` - `<filtering>` should be `false`
- BOM pom.xml sets this globally
- No individual POM should override with `<filtering>true</filtering>`

### Problem: Configuration not picked up in IntelliJ

**Solution:**
1. Edit Run Configuration
2. Go to "Environment Variables"
3. Click "..." to open editor
4. Add variables manually OR
5. Check "Include system environment variables"
6. Export variables in shell before starting IntelliJ

## Migration from Old System

Old system used:
- Maven resource filtering
- `config.properties` (local, git-ignored)
- Lowercase property names
- Build-time value replacement

New system uses:
- Environment variables
- `.env` file (local, git-ignored)
- UPPERCASE variable names
- Runtime value resolution

**No migration needed** - just create `.env` file and rebuild.

## Best Practices

1. ✅ **DO** use UPPERCASE environment variable names
2. ✅ **DO** provide sensible fallback values in `microprofile-config.properties`
3. ✅ **DO** keep `.env` file git-ignored
4. ✅ **DO** update `.env.template` when adding new variables
5. ❌ **DON'T** use Maven resource filtering
6. ❌ **DON'T** commit passwords to git
7. ❌ **DON'T** use lowercase variable names
8. ❌ **DON'T** hardcode configuration in Java code

## See Also

- [Docker Compose .env file documentation](https://docs.docker.com/compose/environment-variables/)
- [MicroProfile Config Specification](https://microprofile.io/project/eclipse/microprofile-config)
