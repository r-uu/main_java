#!/bin/bash
# Gemeinsame WSL Aliase für r-uu Projekt
# Diese Datei wird versioniert und mit allen Entwicklern geteilt

# ═══════════════════════════════════════════════════════════════════
# Projekt-Pfade
# ═══════════════════════════════════════════════════════════════════
export RUU_HOME="/home/r-uu/develop/github/main"
export RUU_BOM="$RUU_HOME/bom"
export RUU_ROOT="$RUU_HOME/root"
export RUU_CONFIG="$RUU_HOME/config"
export RUU_DOCKER="$RUU_CONFIG/shared/docker"
export RUU_WSL="$RUU_CONFIG/shared/wsl"
export RUU_JASPER="$RUU_ROOT/sandbox/office/microsoft/word/jasperreports"

alias ruu-reload-aliases='source $RUU_WSL/aliases.sh && echo "✅ Aliase neu geladen"'

# ═══════════════════════════════════════════════════════════════════
# Navigation
# ═══════════════════════════════════════════════════════════════════
alias ruu-home='cd $RUU_HOME'
alias ruu-bom='cd $RUU_BOM'
alias ruu-root='cd $RUU_ROOT'
alias ruu-lib='cd $RUU_ROOT/lib'
alias ruu-app='cd $RUU_ROOT/app'
alias ruu-config='cd $RUU_CONFIG'
alias ruu-docker='cd $RUU_DOCKER'
alias ruu-wsl='cd $RUU_WSL'
alias ruu-jasper='cd $RUU_JASPER'

# ═══════════════════════════════════════════════════════════════════
# Maven - Build
# ═══════════════════════════════════════════════════════════════════
# Build-Funktion (empfohlen!)
ruu-build-all() {
    local current_dir=$(pwd)
    cd "$RUU_HOME" || return 1
    ./config/shared/scripts/build-all.sh "$@"
    local exit_code=$?
    cd "$current_dir"
    return $exit_code
}

alias ruu-build='ruu-build-all'
alias ruu-clean='cd $RUU_HOME && mvn clean'
alias ruu-install='cd $RUU_HOME && mvn clean install'
alias ruu-install-fast='cd $RUU_HOME && mvn clean install -DskipTests'
alias ruu-test='cd $RUU_HOME && mvn test'
alias ruu-test-module='mvn test'  # Run in current directory
alias ruu-verify='cd $RUU_HOME && mvn verify'

# Einzelne Module
alias ruu-bom-install='cd $RUU_BOM && mvn clean install'
alias ruu-root-install='cd $RUU_ROOT && mvn clean install'
alias ruu-lib-install='cd $RUU_ROOT/lib && mvn clean install'
alias ruu-app-install='cd $RUU_ROOT/app && mvn clean install'

# ═══════════════════════════════════════════════════════════════════
# Docker - Daemon
# ═══════════════════════════════════════════════════════════════════
alias ruu-docker-daemon-start='sudo service docker start'
alias ruu-docker-daemon-stop='sudo service docker stop'
alias ruu-docker-daemon-status='sudo service docker status'
alias ruu-docker-daemon-restart='sudo service docker restart'

# ═══════════════════════════════════════════════════════════════════
# Docker - Services (alle Container)
# ═══════════════════════════════════════════════════════════════════
alias ruu-docker-up='cd $RUU_DOCKER && docker compose up -d'
alias ruu-docker-down='cd $RUU_DOCKER && docker compose down'
alias ruu-docker-restart='ruu-docker-down && ruu-docker-up'
alias ruu-docker-logs='cd $RUU_DOCKER && docker compose logs -f'
alias ruu-docker-ps='docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"'
alias ruu-docker-cleanup='docker container prune -f && docker volume prune -f && echo "✅ Container und Volumes bereinigt"'
alias ruu-docker-reset='bash $RUU_DOCKER/complete-reset.sh'
alias ruu-docker-setup='bash $RUU_DOCKER/complete-setup.sh'

# Docker - Starte alle Container und warte bis sie bereit sind
alias ruu-docker-start-all='bash $RUU_DOCKER/start-all-and-wait.sh'

# Docker - Automatischer Systemstart (startet alle Services und wartet bis ready)
alias ruu-docker-startup='bash $RUU_DOCKER/startup-and-setup.sh'
alias ruu-docker-status='bash $RUU_DOCKER/check-status.sh'

# Docker - Test & Verifikation
alias ruu-docker-test='bash $RUU_CONFIG/shared/scripts/test-docker-autostart.sh'
alias ruu-docker-test-build='bash $RUU_CONFIG/shared/scripts/test-docker-autostart.sh --with-build'
alias ruu-docker-test-multidb='bash $RUU_CONFIG/shared/scripts/test-multi-db.sh'

# ═══════════════════════════════════════════════════════════════════
# Docker - PostgreSQL (JEEERAaH)
# ═══════════════════════════════════════════════════════════════════
alias ruu-postgres-start='cd $RUU_DOCKER && docker compose up -d postgres-jeeeraaah'
alias ruu-postgres-stop='docker container stop postgres-jeeeraaah'
alias ruu-postgres-restart='ruu-postgres-stop && ruu-postgres-start'
alias ruu-postgres-logs='docker logs -f postgres-jeeeraaah'
alias ruu-postgres-shell='docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah'
alias ruu-postgres-shell-admin='docker exec -it postgres-jeeeraaah psql -U postgres'

# PostgreSQL - Datenbank-Reparatur
# Erstellt lib_test Datenbank falls sie nicht existiert (für Library-Tests)
alias ruu-postgres-ensure-lib-test='docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "CREATE DATABASE lib_test OWNER postgres_jeeeraaah_username;" 2>/dev/null && echo "✅ lib_test erstellt" || echo "✅ lib_test existiert bereits"'
alias ruu-postgres-reset-lib-test='docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "DROP DATABASE IF EXISTS lib_test;" && docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "CREATE DATABASE lib_test OWNER postgres_jeeeraaah_username;" && echo "✅ lib_test neu erstellt"'

# ═══════════════════════════════════════════════════════════════════
# Docker - Keycloak
# ═══════════════════════════════════════════════════════════════════
alias ruu-keycloak-start='cd $RUU_DOCKER && docker compose up -d keycloak'
alias ruu-keycloak-stop='docker container stop keycloak'
alias ruu-keycloak-restart='ruu-keycloak-stop && ruu-keycloak-start'
alias ruu-keycloak-logs='docker logs -f keycloak'
alias ruu-keycloak-admin='echo "Keycloak Admin: http://localhost:8080/admin (User: keycloak_admin_username / keycloak_admin_password)"'

# Keycloak Realm Setup (lädt .env vor Ausführung)
alias ruu-keycloak-setup='cd $RUU_ROOT/lib/keycloak.admin && mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -Dkeycloak.admin.user=admin -Dkeycloak.admin.password=admin'

# Keycloak - Komplett-Reset (Container + Volume neu erstellen, dann Realm)
ruu-keycloak-reset() {
    echo "��� Keycloak Komplett-Reset..."
    bash "$RUU_DOCKER/reset-keycloak.sh"
}

# Keycloak - Nur Realm neu erstellen (ohne Container-Reset)
ruu-keycloak-realm-reset() {
    echo "🔄 Keycloak Realm neu erstellen..."
    cd "$RUU_ROOT/lib/keycloak.admin" || return 1
    source "$RUU_DOCKER/.env"
    mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -q
    echo "✅ Keycloak Realm neu erstellt!"
}

# ═══════════════════════════════════════════════════════════════════
# Docker - JasperReports
# ═══════════════════════════════════════════════════════════════════
alias ruu-jasper-start='cd $RUU_DOCKER && docker compose up -d jasperreports'
alias ruu-jasper-stop='docker container stop jasperreports'
alias ruu-jasper-restart='ruu-jasper-stop && ruu-jasper-start'
alias ruu-jasper-logs='docker logs -f jasperreports'
alias ruu-jasper-shell='docker exec -it jasperreports sh'
alias ruu-jasper-rebuild='cd $RUU_DOCKER && docker compose build jasperreports && docker compose up -d jasperreports'
alias ruu-jasper-test='curl http://localhost:8090/health'

# ═══════════════════════════════════════════════════════════════════
# Git
# ═══════════════════════════════════════════════════════════════════
alias ruu-status='cd $RUU_HOME && git status'
alias ruu-pull='cd $RUU_HOME && git pull'
alias ruu-push='cd $RUU_HOME && git push'
alias ruu-log='cd $RUU_HOME && git log --oneline --graph --all -20'
alias ruu-diff='cd $RUU_HOME && git diff'
alias ruu-branches='cd $RUU_HOME && git branch -a'
alias ruu-git-fix='bash $RUU_CONFIG/shared/scripts/fix-git-wsl-intellij.sh'

# ═══════════════════════════════════════════════════════════════════
# System & Monitoring
# ═══════════════════════════════════════════════════════════════════
alias ruu-ports='netstat -tulpn 2>/dev/null | grep LISTEN'
alias ruu-disk='df -h | grep -E "(Filesystem|/home)"'
alias ruu-tree='cd $RUU_HOME && tree -L 3 -I target'
alias ruu-tree-full='cd $RUU_HOME && tree -I target'

# ═══════════════════════════════════════════════════════════════════
# Java & Tools Versionen
# ═══════════════════════════════════════════════════════════════════
alias ruu-java-version='java --version'
alias ruu-maven-version='mvn --version'
alias ruu-docker-version='docker --version && docker compose version'
alias ruu-graalvm-version='echo "GraalVM: $(java --version | head -n1)" && echo "Path: $JAVA_HOME"'
alias ruu-versions='echo "=== Tool Versionen ===" && ruu-java-version && echo "" && ruu-maven-version && echo "" && ruu-docker-version'

# ═══════════════════════════════════════════════════════════════════
# IntelliJ IDEA
# ═══════════════════════════════════════════════════════════════════
alias ruu-intellij-fix='bash $RUU_CONFIG/shared/scripts/fix-intellij-indexing.sh'

# ═══════════════════════════════════════════════════════════════════
# Shell & Aliase
# ═══════════════════════════════════════════════════════════════════
alias ruu-shell-reset='clear && exec $SHELL'
alias ruu-aliases-reload='source ~/.bashrc'
alias ruu-aliases-edit='${EDITOR:-nano} $RUU_HOME/config/shared/wsl/aliases.sh'

# ═══════════════════════════════════════════════════════════════════
# Hilfe & Dokumentation
# ═══════════════════════════════════════════════════════════════════
alias ruu-help='cat $RUU_HOME/config/shared/wsl/aliases.sh | grep "^alias ruu-" | sed "s/alias //" | sed "s/=/\t→ /" | sort | column -t -s "→"'
alias ruu-docs='ls -1 $RUU_HOME/config/*.md && echo "" && cat $RUU_HOME/START-HERE.md'

# ═══════════════════════════════════════════════════════════════════
# Application - Frontend Runner (uses Maven exec:java for correct JPMS module path)
# ═══════════════════════════════════════════════════════════════════
alias ruu-dash='cd $RUU_ROOT/app/jeeeraaah/frontend/ui/fx && mvn exec:java'

# ═══════════════════════════════════════════════════════════════════
# Application - Backend (Open Liberty)
# ═══════════════════════════════════════════════════════════════════
alias ruu-liberty-start='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev'
alias ruu-liberty-run='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:run'
alias ruu-liberty-stop='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:stop'

# ═══════════════════════════════════════════════════════════════════
# Initialisierung & Git-Kompatibilität
# ═══════════════════════════════════════════════════════════════════
# Verhindert "Exec format error" wenn IntelliJ versucht Windows .exe aus WSL auszuführen
export GIT_ASKPASS=""
export SSH_ASKPASS=""

echo "✓ r-uu Projekt-Aliase geladen"
echo "  📚 Hilfe: ruu-help | ruu-docs"
echo "  🚀 Quick Start: ruu-docker-startup"
echo "  🔨 Build: ruu-build"
echo "  🐳 Docker: ruu-docker-ps"
echo "  🖥️  Backend: ruu-liberty-start"
echo "  🎨 Frontend: ruu-dash"
