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
export RUU_JASPER="$RUU_ROOT/sandbox/office/microsoft/word/jasperreports"

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
alias ruu-docker-reset='bash $RUU_DOCKER/reset-all-containers.sh'

# ═══════════════════════════════════════════════════════════════════
# Docker - PostgreSQL (JEEERAaH)
# ═══════════════════════════════════════════════════════════════════
alias ruu-postgres-start='cd $RUU_DOCKER && docker compose up -d postgres-jeeeraaah'
alias ruu-postgres-stop='docker container stop postgres-jeeeraaah'
alias ruu-postgres-restart='ruu-postgres-stop && ruu-postgres-start'
alias ruu-postgres-logs='docker logs -f postgres-jeeeraaah'
alias ruu-postgres-shell='docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah'
alias ruu-postgres-shell-admin='docker exec -it postgres-jeeeraaah psql -U postgres'

# ═══════════════════════════════════════════════════════════════════
# Docker - Keycloak
# ═══════════════════════════════════════════════════════════════════
alias ruu-keycloak-start='cd $RUU_DOCKER && docker compose up -d keycloak'
alias ruu-keycloak-stop='docker container stop keycloak-service'
alias ruu-keycloak-restart='ruu-keycloak-stop && ruu-keycloak-start'
alias ruu-keycloak-logs='docker logs -f keycloak-service'
alias ruu-keycloak-admin='echo "Keycloak Admin: http://localhost:8080/admin (siehe config.properties)"'

# ═══════════════════════════════════════════════════════════════════
# Docker - JasperReports
# ═══════════════════════════════════════════════════════════════════
alias ruu-jasper-start='cd $RUU_DOCKER && docker compose up -d jasperreports'
alias ruu-jasper-stop='docker container stop jasperreports-service'
alias ruu-jasper-restart='ruu-jasper-stop && ruu-jasper-start'
alias ruu-jasper-logs='docker logs -f jasperreports-service'
alias ruu-jasper-shell='docker exec -it jasperreports-service sh'
alias ruu-jasper-rebuild='cd $RUU_DOCKER && docker compose build jasperreports && docker compose up -d jasperreports'
alias ruu-jasper-test='curl http://localhost:8090/health'
alias ruu-jasper-cleanup='bash $RUU_CONFIG/shared/scripts/cleanup-jasperreports.sh'

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
# Shell & Aliase
# ═══════════════════════════════════════════════════════════════════
alias ruu-shell-reset='clear && exec $SHELL'
alias ruu-aliases-reload='source ~/.bashrc'
alias ruu-aliases-edit='${EDITOR:-nano} $RUU_HOME/config/shared/wsl/aliases.sh'

# ═══════════════════════════════════════════════════════════════════
# Hilfe & Dokumentation
# ═══════════════════════════════════════════════════════════════════
alias ruu-help='echo "=== r-uu Projekt Aliase ===" && echo "" && cat $RUU_HOME/config/shared/wsl/aliases.sh | grep "^alias ruu-" | sed "s/alias /  /" | sed "s/=/ → /" | sort'
alias ruu-help-docker='echo "=== Docker Aliase ===" && ruu-help | grep docker'
alias ruu-help-maven='echo "=== Maven Aliase ===" && ruu-help | grep -E "(build|install|clean|test|verify)"'
alias ruu-help-nav='echo "=== Navigation Aliase ===" && ruu-help | grep -E "(home|bom|root|lib|app|config|docker|jasper)\s"'
alias ruu-help-git='echo "=== Git Aliase ===" && ruu-help | grep git'
alias ruu-docs='cd $RUU_HOME/config && cat INDEX.md 2>/dev/null || ls -la *.md'
alias ruu-quickstart='cd $RUU_HOME/config && cat QUICKSTART.md 2>/dev/null || cat START-HERE.md 2>/dev/null || echo "Siehe config/*.md Dateien"'

# ═══════════════════════════════════════════════════════════════════
# Initialisierung & Git-Kompatibilität
# ═══════════════════════════════════════════════════════════════════
# Verhindert "Exec format error" wenn IntelliJ versucht Windows .exe aus WSL auszuführen
export GIT_ASKPASS=""
export SSH_ASKPASS=""

echo "✓ r-uu Projekt-Aliase geladen"
echo "  Hilfe: ruu-help | Build: ruu-build | Docker: ruu-docker-ps"
