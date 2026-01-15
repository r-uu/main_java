# 📋 config/ - Gesamtübersicht

## Was ist config/?

Das `config/` Verzeichnis verwaltet alle entwicklungsbezogenen Konfigurationen für das r-uu Projekt.

## 🎯 Problem & Lösung

### Problem
- Jeder Entwickler hat unterschiedliche Entwicklungsmaschinen
- Gemeinsame Konfigurationen sollen geteilt werden
- Secrets und maschinenspezifische Settings dürfen NICHT ins Git
- Konsistente Entwicklungsumgebung für alle

### Lösung
```
config/
├── shared/     → Gemeinsam, versioniert (✅ Git)
├── local/      → Individuell, nicht versioniert (❌ Git)  
└── templates/  → Vorlagen für local/ (✅ Git)
```

## 📦 Was ist enthalten?

### 1. WSL/Bash Konfiguration

**Gemeinsam** (`config/shared/wsl/aliases.sh`):
```bash
# Projekt-weite Aliase
alias cdruu='cd /home/r-uu/develop/github/main'
alias ruu-install='cd $RUU_HOME && mvn clean install'
```

**Individuell** (`config/local/wsl/aliases.sh`):
```bash
# Deine persönlichen Aliase
alias mein-alias='mein-command'
export MY_SECRET_KEY='...'
```

### 2. Docker Konfiguration

**Gemeinsam** (`config/shared/docker/docker-compose.yml`):
```yaml
# Standard Docker Services
services:
  postgres:
    image: postgres:16-alpine
    # ...
```

**Individuell** (`config/local/docker/.env.local`):
```env
POSTGRES_PASSWORD=MEIN_SICHERES_PASSWORT
POSTGRES_PORT=5433  # Anderer Port wegen Konflikt
```

### 3. Build-Skripte

**Gemeinsam** (`config/shared/scripts/`):
- `setup-dev-env.sh` - Einmalige Einrichtung
- `maven-build.sh` - Maven Build Helper

### 4. AI-Prompts

**Gemeinsam** (`config/shared/ai-prompts/`):
- `maven/` - Maven-spezifische Prompts
- `code-review/` - Code-Review Templates
- `documentation/` - Dokumentations-Prompts
- usw.

## 🚀 Schnellstart

### Neue Entwicklungsmaschine

```bash
# 1. Setup ausführen
cd config/shared/scripts
./setup-dev-env.sh

# 2. Lokale Config anpassen
nano config/local/docker/.env.local

# 3. Shell-Config aktivieren
echo "source ~/develop/github/main/config/shared/wsl/aliases.sh" >> ~/.bashrc
echo "source ~/develop/github/main/config/local/wsl/aliases.sh" >> ~/.bashrc
source ~/.bashrc

# 4. Los geht's!
ruu-docker-up
ruu-install
```

## 📚 Wichtige Dokumentation

| Datei | Beschreibung |
|-------|--------------|
| [QUICKSTART.md](QUICKSTART.md) | 🚀 Schnellstart-Anleitung |
| [readme.md](readme.md) | 📖 Vollständige Dokumentation |
| [STRUCTURE.md](STRUCTURE.md) | 🗂️ Detaillierte Struktur |
| [GRAALVM-INSTALLATION.md](GRAALVM-INSTALLATION.md) | 🔥 GraalVM 25 Installation |
| [../GIT-PUSH-FINAL-SOLUTION.md](../GIT-PUSH-FINAL-SOLUTION.md) | 🔐 Git Push in IntelliJ (WSL) |
| [GRAALVM-25-MIGRATION.md](GRAALVM-25-MIGRATION.md) | ✅ GraalVM 25 Migration (2026-01-11) |
| [INTELLIJ-WSL-SETUP.md](INTELLIJ-WSL-SETUP.md) | 🔧 IntelliJ IDEA WSL Setup |

## 🔐 Sicherheit

### ✅ Was ins Git darf
- Standard-Konfigurationen ohne Secrets
- Templates mit Platzhaltern
- Dokumentation
- Scripts

### ❌ Was NICHT ins Git darf
- Passwörter, API-Keys, Tokens
- Maschinenspezifische Pfade
- Lokale Datenbank-Dumps
- Persönliche Einstellungen

→ Siehe `.gitignore` für Details

## 🛠️ Typische Workflows

### Neue gemeinsame Konfiguration hinzufügen

```bash
# 1. In shared/ erstellen/bearbeiten
echo "alias new-alias='command'" >> config/shared/wsl/aliases.sh

# 2. Committen
git add config/shared/
git commit -m "Add new shared alias"
git push

# 3. Andere Entwickler holen sich das Update
git pull
source ~/.bashrc  # Aliase neu laden
```

### Lokale Anpassung vornehmen

```bash
# Einfach local/ bearbeiten - wird nie committed
nano config/local/wsl/aliases.sh
source config/local/wsl/aliases.sh
```

### Neues Template erstellen

```bash
# 1. Template erstellen
cat > config/templates/my-tool.template << 'EOF'
# My Tool Configuration
SETTING=default_value
EOF

# 2. Committen
git add config/templates/my-tool.template
git commit -m "Add my-tool configuration template"

# 3. Entwickler kopieren zu local/
cp config/templates/my-tool.template config/local/my-tool.conf
nano config/local/my-tool.conf
```

## 💡 Best Practices

1. **Defaults in shared/, Overrides in local/**
   - Standard-Werte → `shared/`
   - Maschinenspezifisch → `local/`

2. **Templates für wiederkehrende Configs**
   - Neue Config-Art → Template erstellen
   - Gut dokumentieren
   - Beispielwerte zeigen

3. **Nie Secrets committen**
   - Verwende Platzhalter wie `CHANGE_ME`
   - Dokumentiere wo Secrets hinkommen
   - Prüfe vor Commit: `git diff`

4. **Scripts wartbar halten**
   - Kommentare hinzufügen
   - Fehlerbehandlung einbauen
   - Help-Funktion bereitstellen

## 🤝 Für neue Teammitglieder

1. Repository klonen
2. `config/shared/scripts/setup-dev-env.sh` ausführen
3. `config/local/` Dateien anpassen
4. `QUICKSTART.md` folgen

→ Innerhalb 5 Minuten einsatzbereit!

## ❓ Häufige Fragen

**Q: Wo speichere ich mein Datenbank-Passwort?**  
A: In `config/local/docker/.env.local` (wird von Git ignoriert)

**Q: Ich habe einen nützlichen Alias - wo hin damit?**  
A: Wenn für alle nützlich → `config/shared/wsl/aliases.sh`  
   Wenn nur für dich → `config/local/wsl/aliases.sh`

**Q: Kann ich die Verzeichnisstruktur ändern?**  
A: Klar! Aber passe dann auch `.gitignore`, `setup-dev-env.sh` und die Dokumentation an.

**Q: Was wenn config/local/ nicht existiert?**  
A: Führe `config/shared/scripts/setup-dev-env.sh` aus.

## 🔄 Updates & Wartung

### Nach Git Pull
```bash
# Prüfe ob neue Templates verfügbar sind
ls config/templates/

# Vergleiche mit deinen local/ Dateien
diff config/templates/.env.template config/local/docker/.env.local

# Bei Bedarf neue Einstellungen übernehmen
```

### Config-Ordner aufräumen
```bash
# Entferne nicht mehr benötigte lokale Configs
rm -rf config/local/old-stuff/
```

---

**Zuletzt aktualisiert:** 2026-01-11  
**Maintainer:** r-uu

