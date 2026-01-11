#!/bin/bash
# Gemeinsame WSL Aliase für r-uu Projekt
# Diese Datei wird versioniert und mit allen Entwicklern geteilt

# ═══════════════════════════════════════════════════════════════════
# Java / GraalVM
# ═══════════════════════════════════════════════════════════════════
export GRAALVM_HOME="/opt/graalvm-jdk-25"
export JAVA_HOME="$GRAALVM_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# ═══════════════════════════════════════════════════════════════════
# Projekt-Pfade
# ═══════════════════════════════════════════════════════════════════
export RUU_HOME="/home/r-uu/develop/github/main"
export RUU_BOM="$RUU_HOME/bom"
export RUU_ROOT="$RUU_HOME/root"
export RUU_CONFIG="$RUU_HOME/config"
export RUU_DOCKER="$RUU_CONFIG/shared/docker"

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

# Shortcuts (kurz)
alias cdruu='cd $RUU_HOME'
alias cdbom='cd $RUU_BOM'
alias cdroot='cd $RUU_ROOT'
alias cdlib='cd $RUU_ROOT/lib'
alias cdapp='cd $RUU_ROOT/app'

# ═══════════════════════════════════════════════════════════════════
# Maven
# ═══════════════════════════════════════════════════════════════════
alias ruu-clean='cd $RUU_HOME && mvn clean'
alias ruu-install='cd $RUU_HOME && mvn clean install'
alias ruu-install-fast='cd $RUU_HOME && mvn clean install -DskipTests'
alias ruu-test='cd $RUU_HOME && mvn test'
alias ruu-verify='cd $RUU_HOME && mvn verify'

# Build-Skripte (empfohlen!)
# Als Funktion, damit aktuelles Verzeichnis wiederhergestellt wird
build-all() {
    local current_dir=$(pwd)
    cd "$RUU_HOME" || return 1
    ./config/shared/scripts/build-all.sh "$@"
    local exit_code=$?
    cd "$current_dir"
    return $exit_code
}
alias ruu-build='build-all'
alias ruu-build-all='build-all'

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
alias ruu-docker-system-prune='sudo docker system prune --all --volumes --force && sudo docker volume prune --all --force'

# ═══════════════════════════════════════════════════════════════════
# Docker - Services (alle)
# ═══════════════════════════════════════════════════════════════════
alias ruu-docker-up='cd $RUU_DOCKER && docker-compose up -d'
alias ruu-docker-down='cd $RUU_DOCKER && docker-compose down'
alias ruu-docker-restart='ruu-docker-down && ruu-docker-up'
alias ruu-docker-logs='cd $RUU_DOCKER && docker-compose logs -f'
alias ruu-docker-ps='docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"'

# ═══════════════════════════════════════════════════════════════════
# Docker - PostgreSQL
# ═══════════════════════════════════════════════════════════════════
# PostgreSQL Service (lokal, nicht Docker)
alias ruu-postgres-service-start='sudo service postgresql start'
alias ruu-postgres-service-stop='sudo service postgresql stop'
alias ruu-postgres-service-status='sudo service postgresql status'
alias ruu-postgres-service-restart='sudo service postgresql restart'
alias ruu-postgres-setup='sudo $RUU_HOME/config/shared/scripts/setup-postgresql.sh'

# PostgreSQL Docker (postgres-jeeeraaah Container)
alias ruu-postgres-docker-setup='docker exec -it postgres-jeeeraaah psql -U postgres -c "CREATE USER r_uu WITH PASSWORD '"'"'r_uu_password'"'"';" 2>/dev/null || docker exec -it postgres-jeeeraaah psql -U postgres -c "ALTER USER r_uu WITH PASSWORD '"'"'r_uu_password'"'"';" && docker exec -it postgres-jeeeraaah psql -U postgres -c "CREATE DATABASE lib_test OWNER r_uu;" 2>/dev/null || true && docker exec -it postgres-jeeeraaah psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE lib_test TO r_uu;" && docker exec -it postgres-jeeeraaah psql -U postgres -d lib_test -c "GRANT ALL ON SCHEMA public TO r_uu;"'
alias ruu-postgres-docker-shell='docker exec -it postgres-jeeeraaah psql -U postgres'
alias ruu-postgres-docker-shell-ruu='docker exec -it postgres-jeeeraaah psql -U r_uu -d lib_test'

# PostgreSQL Docker
alias ruu-postgres-start='cd $RUU_DOCKER && docker-compose up -d postgres'
alias ruu-postgres-stop='docker container stop ruu-postgres'
alias ruu-postgres-restart='ruu-postgres-stop && ruu-postgres-start'
alias ruu-postgres-logs='docker logs -f ruu-postgres'
alias ruu-postgres-shell='docker exec -it ruu-postgres psql -U ${POSTGRES_USER:-ruu} -d ${POSTGRES_DB:-ruu_dev}'
alias ruu-postgres-rebuild='ruu-postgres-stop && docker volume rm ruu-postgres-data 2>/dev/null; ruu-postgres-start'

# PostgreSQL Client (lokal)
alias ruu-psql='PGPASSWORD=r_uu_password psql -h localhost -U r_uu -d lib_test'

# ═══════════════════════════════════════════════════════════════════
# Docker - Keycloak
# ═══════════════════════════════════════════════════════════════════
alias ruu-keycloak-start='cd $RUU_DOCKER && docker-compose up -d keycloak'
alias ruu-keycloak-stop='docker container stop ruu-keycloak'
alias ruu-keycloak-restart='ruu-keycloak-stop && ruu-keycloak-start'
alias ruu-keycloak-logs='docker logs -f ruu-keycloak'
alias ruu-keycloak-admin='echo "Keycloak Admin: http://localhost:8080/admin (admin/admin)"'

# ═══════════════════════════════════════════════════════════════════
# Git
# ═══════════════════════════════════════════════════════════════════
alias ruu-status='cd $RUU_HOME && git status'
alias ruu-pull='cd $RUU_HOME && git pull'
alias ruu-push='cd $RUU_HOME && git push'
alias ruu-log='cd $RUU_HOME && git log --oneline --graph --all -20'
alias ruu-diff='cd $RUU_HOME && git diff'
alias ruu-branches='cd $RUU_HOME && git branch -a'

# ═══════════════════════════════════════════════════════════════════
# Shell & System
# ═══════════════════════════════════════════════════════════════════
alias ruu-shell-reset='clear && exec $SHELL'
alias ruu-aliases-reload='source ~/.bashrc'
alias ruu-tree='cd $RUU_HOME && tree -L 3 -I target'
alias ruu-tree-full='cd $RUU_HOME && tree -I target'
alias ruu-disk='df -h | grep -E "(Filesystem|/home)"'

# ═══════════════════════════════════════════════════════════════════
# Entwicklung & Debug
# ═══════════════════════════════════════════════════════════════════
alias ruu-ports='netstat -tulpn 2>/dev/null | grep LISTEN'
alias ruu-java-version='java --version'
alias ruu-maven-version='mvn --version'
alias ruu-docker-version='docker --version && docker-compose --version'

# GraalVM-spezifische Befehle
alias ruu-graalvm-version='echo "GraalVM: $(java --version | head -n1)" && echo "Path: $JAVA_HOME"'
alias ruu-native-image='native-image'
alias ruu-gu='gu'
alias ruu-java-check='bash $RUU_CONFIG/shared/scripts/check-java.sh'

# ═══════════════════════════════════════════════════════════════════
# Hilfe & Dokumentation
# ═══════════════════════════════════════════════════════════════════
alias ruu-help='echo "=== r-uu Aliase ===" && cat $RUU_HOME/config/shared/wsl/aliases.sh | grep "^alias" | sed "s/alias //" | column -t -s "=" | sort'
alias ruu-help-docker='echo "Docker Aliase:" && ruu-help | grep docker'
alias ruu-help-maven='echo "Maven Aliase:" && ruu-help | grep -E "install|clean|test|verify"'
alias ruu-help-nav='echo "Navigation Aliase:" && ruu-help | grep -E "^cd|^ruu-(home|bom|root|lib|app|config|docker)"'
alias ruu-docs='cd $RUU_HOME/config && cat INDEX.md'
alias ruu-quickstart='cd $RUU_HOME/config && cat QUICKSTART.md'

# ═══════════════════════════════════════════════════════════════════
# Initialisierung
# ═══════════════════════════════════════════════════════════════════
echo "✓ r-uu gemeinsame Aliase geladen"
echo "  Hilfe: ruu-help | Build: build-all | PostgreSQL Setup: ruu-postgres-setup"

# ═══════════════════════════════════════════════════════════════════
# Git - WSL + IntelliJ Kompatibilität
# ═══════════════════════════════════════════════════════════════════
# Verhindert "Exec format error" wenn IntelliJ versucht Windows .exe aus WSL auszuführen
export GIT_ASKPASS=""
export SSH_ASKPASS=""
# Git Konfiguration fix-Skript
alias ruu-git-fix='bash $RUU_CONFIG/shared/scripts/fix-git-wsl-intellij.sh'
