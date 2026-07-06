#!/bin/bash
# Project-specific WSL aliases for java/main.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/java/main

# ═══════════════════════════════════════════════════════════════════
# Project Paths
# ═══════════════════════════════════════════════════════════════════
export RUU_MAIN="/home/r-uu/develop/github/java/main"
export RUU_BOM="$RUU_MAIN/bom"
export RUU_ROOT="$RUU_MAIN/root"
export RUU_CONFIG="$RUU_MAIN/config"
export RUU_DOCKER="$RUU_CONFIG/shared/docker"
export RUU_WSL="$RUU_CONFIG/shared/wsl"
export RUU_JASPER="$RUU_ROOT/sandbox/office/microsoft/word/jasperreports"

# ═══════════════════════════════════════════════════════════════════
# Navigation
# ═══════════════════════════════════════════════════════════════════
alias ruu-cd-home='cd $RUU_MAIN'
alias ruu-cd-bom='cd $RUU_BOM'
alias ruu-cd-root='cd $RUU_ROOT'
alias ruu-cd-lib='cd $RUU_ROOT/lib'
alias ruu-cd-app='cd $RUU_ROOT/app'
alias ruu-cd-config='cd $RUU_CONFIG'
alias ruu-cd-docker='cd $RUU_DOCKER'
alias ruu-cd-wsl='cd $RUU_WSL'
alias ruu-cd-jasper='cd $RUU_JASPER'

# ═══════════════════════════════════════════════════════════════════
# Maven - Build
# ═══════════════════════════════════════════════════════════════════
ruu-mvn-build-all() {
    local current_dir=$(pwd)
    cd "$RUU_MAIN" || return 1
    ./config/shared/scripts/build-all.sh "$@"
    local exit_code=$?
    cd "$current_dir"
    return $exit_code
}

alias ruu-mvn-build='ruu-mvn-build-all'
alias ruu-mvn-clean='cd $RUU_ROOT && mvn clean'
alias ruu-mvn-install='cd $RUU_ROOT && mvn liberty:stop -pl app/jeeeraaah/backend/api/ws_rs --fail-at-end; mvn clean install'
alias ruu-mvn-install-fast='cd $RUU_ROOT && mvn liberty:stop -pl app/jeeeraaah/backend/api/ws_rs --fail-at-end; mvn clean install -DskipTests'
alias ruu-mvn-test='cd $RUU_ROOT && mvn test'
alias ruu-mvn-test-module='mvn test'
alias ruu-mvn-verify='cd $RUU_ROOT && mvn verify'
alias ruu-mvn-bom-install='cd $RUU_BOM && mvn clean install'
alias ruu-mvn-root-install='cd $RUU_ROOT && mvn clean install'
alias ruu-mvn-lib-install='cd $RUU_ROOT/lib && mvn clean install'
alias ruu-mvn-app-install='cd $RUU_ROOT/app && mvn clean install'

# ═══════════════════════════════════════════════════════════════════
# Docker - Daemon
# ═══════════════════════════════════════════════════════════════════
alias ruu-docker-daemon-start='sudo service docker start'
alias ruu-docker-daemon-stop='sudo service docker stop'
alias ruu-docker-daemon-status='sudo service docker status'
alias ruu-docker-daemon-restart='sudo service docker restart'

# ═══════════════════════════════════════════════════════════════════
# Docker - Services
# ═══════════════════════════════════════════════════════════════════
alias ruu-docker-up='cd $RUU_DOCKER && docker compose up -d'
alias ruu-docker-down='cd $RUU_DOCKER && docker compose down'
alias ruu-docker-restart='ruu-docker-down && ruu-docker-up'
alias ruu-docker-logs='cd $RUU_DOCKER && docker compose logs -f'
alias ruu-docker-ps='docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"'
alias ruu-docker-cleanup='docker container prune -f && docker volume prune -f && echo "✅ Containers and volumes cleaned up"'
alias ruu-docker-reset='bash $RUU_DOCKER/complete-reset.sh'
alias ruu-docker-setup='bash $RUU_DOCKER/complete-setup.sh'
alias ruu-docker-start-all='bash $RUU_DOCKER/start-all-and-wait.sh'
alias ruu-docker-startup='bash $RUU_DOCKER/startup-and-setup.sh'
alias ruu-docker-status='bash $RUU_DOCKER/check-status.sh'
alias ruu-docker-test='bash $RUU_CONFIG/shared/scripts/test-docker-autostart.sh'
alias ruu-docker-test-build='bash $RUU_CONFIG/shared/scripts/test-docker-autostart.sh --with-build'
alias ruu-docker-test-multidb='bash $RUU_CONFIG/shared/scripts/test-multi-db.sh'

# ═══════════════════════════════════════════════════════════════════
# Docker - PostgreSQL
# ═══════════════════════════════════════════════════════════════════
alias ruu-pg-start='cd $RUU_DOCKER && docker compose up -d postgres-jeeeraaah'
alias ruu-pg-stop='docker container stop postgres-jeeeraaah'
alias ruu-pg-restart='ruu-pg-stop && ruu-pg-start'
alias ruu-pg-logs='docker logs -f postgres-jeeeraaah'
alias ruu-pg-shell='docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah'
alias ruu-pg-shell-admin='docker exec -it postgres-jeeeraaah psql -U postgres'
alias ruu-pg-ensure-lib-test='docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "CREATE DATABASE lib_test OWNER postgres_jeeeraaah_username;" 2>/dev/null && echo "✅ lib_test created" || echo "✅ lib_test already exists"'
alias ruu-pg-reset-lib-test='docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "DROP DATABASE IF EXISTS lib_test;" && docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c "CREATE DATABASE lib_test OWNER postgres_jeeeraaah_username;" && echo "✅ lib_test recreated"'

# ═══════════════════════════════════════════════════════════════════
# Docker - Keycloak
# ═══════════════════════════════════════════════════════════════════
alias ruu-kc-start='cd $RUU_DOCKER && docker compose up -d keycloak'
alias ruu-kc-stop='docker container stop keycloak'
alias ruu-kc-restart='ruu-kc-stop && ruu-kc-start'
alias ruu-kc-logs='docker logs -f keycloak'
alias ruu-kc-admin='echo "Keycloak Admin: http://localhost:8080/admin (User: keycloak_admin_username / keycloak_admin_password)"'
alias ruu-kc-setup='cd $RUU_ROOT/lib/keycloak_admin && mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -Dkeycloak.admin.user=admin -Dkeycloak.admin.password=admin'

ruu-kc-reset() {
    echo "🔄 Keycloak full reset..."
    bash "$RUU_DOCKER/reset-keycloak.sh"
}

ruu-kc-realm-reset() {
    echo "🔄 Recreating Keycloak realm..."
    cd "$RUU_ROOT/lib/keycloak_admin" || return 1
    source "$RUU_DOCKER/.env"
    mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -q
    echo "✅ Keycloak realm recreated!"
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
alias ruu-git-status='cd $RUU_MAIN && git status'
alias ruu-git-pull='cd $RUU_MAIN && git pull'
alias ruu-git-push='cd $RUU_MAIN && git push'
alias ruu-git-log='cd $RUU_MAIN && git log --oneline --graph --all -20'
alias ruu-git-diff='cd $RUU_MAIN && git diff'
alias ruu-git-branches='cd $RUU_MAIN && git branch -a'
alias ruu-git-fix='bash $RUU_CONFIG/shared/scripts/fix-git-wsl-intellij.sh'

# ═══════════════════════════════════════════════════════════════════
# System & Monitoring
# ═══════════════════════════════════════════════════════════════════
alias ruu-ports='netstat -tulpn 2>/dev/null | grep LISTEN'
alias ruu-disk='df -h | grep -E "(Filesystem|/home)"'
alias ruu-tree='cd $RUU_MAIN && tree -L 3 -I target'
alias ruu-tree-full='cd $RUU_MAIN && tree -I target'
alias ruu-ij-fix='bash $RUU_CONFIG/shared/scripts/fix-intellij-wsl-rendering.sh'

# ═══════════════════════════════════════════════════════════════════
# Application - Frontend / Backend
# ═══════════════════════════════════════════════════════════════════
alias ruu-app-dash='cd $RUU_ROOT/app/jeeeraaah/frontend/ui/fx && mvn exec:java'
alias ruu-postgres-util-ui='cd $RUU_ROOT/lib/postgres_util_ui && mvn exec:java'
alias ruu-ol-start='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev'
alias ruu-ol-run='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:run'
alias ruu-ol-stop='cd $RUU_ROOT/app/jeeeraaah/backend/api/ws_rs && mvn liberty:stop'

echo "✓  java/main aliases loaded"
