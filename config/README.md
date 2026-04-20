o# 📚 Projekt-Dokumentation

**Letzte Aktualisierung:** 2026-01-22  
**Status:** ✅ Aufgeräumt & Konsolidiert

---

## 🎯 START HIER!

### 📖 Zentrale Dokumentation

👉 **[PROJEKT-DOKUMENTATION.md](PROJEKT-DOKUMENTATION.md)** - Vollständige Referenz

### 🏠 Hauptdokumentation

👉 **[../README.md](../README.md)** - Projekt-Übersicht & Schnellstart

---

## 📂 DOKUMENTATIONSSTRUKTUR

### Essentiell (täglich benötigt)

| Dokument | Beschreibung |
|----------|--------------|
| [PROJEKT-DOKUMENTATION.md](PROJEKT-DOKUMENTATION.md) | 📚 **Zentrale Referenz** - Start hier! |
| [README.md](README.md) | 📋 Diese Übersicht |

### Technisch (bei Bedarf)

| Dokument | Wann verwenden? |
|----------|----------------|
| [AUTOMATIC-MODULES-DOCUMENTATION.md](AUTOMATIC-MODULES-DOCUMENTATION.md) | JPMS Warnings verstehen |
| [CONFIG-PROPERTIES-GUIDE.md](CONFIG-PROPERTIES-GUIDE.md) | Properties konfigurieren |
| [DOCKER-SETUP.md](DOCKER-SETUP.md) | Docker Container Setup |
| [KEYCLOAK-SETUP.md](KEYCLOAK-SETUP.md) | Keycloak konfigurieren |
| [POSTGRESQL-SETUP.md](POSTGRESQL-SETUP.md) | PostgreSQL Probleme |
| [STRUCTURE.md](STRUCTURE.md) | Projektstruktur verstehen |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Probleme lösen |
| [WSL-AUTO-STARTUP.md](WSL-AUTO-STARTUP.md) | WSL Autostart |

### Historisch

| Verzeichnis | Inhalt |
|-------------|--------|
| [archive/](archive/) | 📦 Alte Dokumente (bei Bedarf) |

---

## ⚡ QUICK COMMANDS

```bash
# Docker Services starten
ruu-startup

# Projekt bauen
build-all

# PostgreSQL Setup
ruu-postgres-setup

# Keycloak Setup  
ruu-keycloak-setup

# Docker Status
ruu-docker-ps
```

---

## 📊 PROJEKT-STATUS

| Aspekt | Status | Details |
|--------|--------|---------|
| **Build** | ✅ SUCCESS | mvn clean install funktioniert |
| **Tests** | ✅ Aktiv | Keine skipTests |
| **JPMS** | ✅ 100% | 47 module-info.java |
| **Docker** | ✅ Automatisiert | ruu-startup Alias |
| **Dokumentation** | ✅ Konsolidiert | Aufgeräumt am 2026-01-22 |
| **Qualität** | ✅ **5.0/5** | Production Ready! ⭐⭐⭐⭐⭐ |

---

## 🔍 WAS IST WO?

### Konfigurationsdateien

```
config/
├── shared/              # ✅ IM GIT - Projekt-weit gültig
│   ├── wsl/
│   │   ├── aliases.sh   # Bash-Aliase für Projekt
│   │   └── startup.sh   # Docker-Startup-Skript
│   ├── docker/
│   │   └── docker-compose.yml
│   └── scripts/
│       └── *.sh         # Setup-Skripte
│
└── local/               # ❌ NICHT IM GIT - Maschinenspezifisch
    └── (Ihre lokalen Configs)
```

### Aliase aktivieren

```bash
# In ~/.bashrc sollte stehen:
source ~/develop/github/java/main/config/shared/wsl/aliases.sh
```

---

## 🆘 HILFE

### Bei Problemen:

1. **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** durchsehen
2. **[archive/](archive/)** nach ähnlichen Problemen suchen
3. Docker neu starten: `ruu-docker-restart`
4. Kompletter Reset: `ruu-startup`

### Häufige Probleme:

| Problem | Lösung |
|---------|--------|
| Docker startet nicht | `ruu-startup` |
| DB nicht erreichbar | `ruu-postgres-setup` |
| Keycloak Fehler | `ruu-keycloak-setup` |
| Build Fehler | `build-all` |

---

**🎉 Alles bereit für produktive Entwicklung!**


