l#!/bin/bash
#
# WSL Startup Script - Ensures complete Docker environment on every WSL start
#
# This script is automatically executed when WSL starts via .bashrc
# It ensures all Docker services are running and fully initialized
#

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd "${SCRIPT_DIR}/../docker" && pwd)"
LOG_FILE="/tmp/wsl-startup-$(date +%Y%m%d-%H%M%S).log"

# Logging functions
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  $*" | tee -a "$LOG_FILE"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ $*" | tee -a "$LOG_FILE"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ $*" | tee -a "$LOG_FILE"
}

log_warn() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  $*" | tee -a "$LOG_FILE"
}

# Check if already running (prevent double execution)
LOCK_FILE="/tmp/wsl-startup.lock"
if [ -f "$LOCK_FILE" ]; then
    # Check if process is still running
    if kill -0 $(cat "$LOCK_FILE") 2>/dev/null; then
        log_warn "Startup already in progress (PID: $(cat "$LOCK_FILE"))"
        exit 0
    else
        # Stale lock file
        rm -f "$LOCK_FILE"
    fi
fi

# Create lock file
echo $$ > "$LOCK_FILE"
trap "rm -f $LOCK_FILE" EXIT

log_info "════════════════════════════════════════════════════════════════"
log_info "WSL Startup - Initializing Docker Environment"
log_info "════════════════════════════════════════════════════════════════"

# 1. Start Docker daemon if not running
log_info "Step 1/5: Checking Docker daemon..."
if ! docker info >/dev/null 2>&1; then
    log_warn "Docker daemon not running - attempting to start..."
    sudo service docker start >/dev/null 2>&1 || {
        log_error "Failed to start Docker daemon"
        exit 1
    }
    sleep 3
fi
log_success "Docker daemon is running"

# 2. Start all containers
log_info "Step 2/5: Starting Docker containers..."
cd "$DOCKER_DIR"
docker compose up -d --quiet-pull 2>&1 | tee -a "$LOG_FILE"
log_success "Containers started"

# 3. Wait for all containers to be healthy
log_info "Step 3/5: Waiting for containers to become healthy..."

wait_for_healthy() {
    local container=$1
    local max_wait=120  # 2 minutes
    local waited=0

    while [ $waited -lt $max_wait ]; do
        local status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "unknown")

        if [ "$status" = "healthy" ]; then
            log_success "$container is healthy"
            return 0
        fi

        sleep 2
        waited=$((waited + 2))

        # Show progress every 10 seconds
        if [ $((waited % 10)) -eq 0 ]; then
            log_info "  Waiting for $container... (${waited}s, status: $status)"
        fi
    done

    log_error "$container did not become healthy within ${max_wait}s"
    return 1
}

# Wait for each container
wait_for_healthy "postgres-jeeeraaah"
wait_for_healthy "postgres-keycloak"
wait_for_healthy "keycloak"
wait_for_healthy "jasperreports"

# 4. Verify databases exist
log_info "Step 4/5: Verifying databases..."

# Check jeeeraaah database
docker exec postgres-jeeeraaah psql -U r_uu -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw jeeeraaah && \
    log_success "Database 'jeeeraaah' exists" || \
    log_error "Database 'jeeeraaah' missing!"

# Check lib_test database
docker exec postgres-jeeeraaah psql -U r_uu -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw lib_test && \
    log_success "Database 'lib_test' exists" || \
    log_error "Database 'lib_test' missing!"

# Check keycloak database
docker exec postgres-keycloak psql -U r_uu -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw keycloak && \
    log_success "Database 'keycloak' exists" || \
    log_error "Database 'keycloak' missing!"

# 5. Initialize Keycloak realm if missing
log_info "Step 5/5: Checking Keycloak realm..."

# Wait a bit for Keycloak to be fully ready
sleep 5

# Check if jeeeraaah-realm exists
if docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms/jeeeraaah-realm \
    --server http://localhost:8080 --realm master --user admin --password admin \
    >/dev/null 2>&1; then
    log_success "Keycloak realm 'jeeeraaah-realm' exists"
else
    log_warn "Keycloak realm 'jeeeraaah-realm' missing - creating now..."

    # Attempt to create realm via KeycloakRealmSetup
    KEYCLOAK_ADMIN_DIR="/home/r-uu/develop/github/java/main/root/lib/keycloak.admin"

    if [ -f "$KEYCLOAK_ADMIN_DIR/pom.xml" ]; then
        log_info "  Running KeycloakRealmSetup.java..."
        cd "$KEYCLOAK_ADMIN_DIR"

        if mvn exec:java \
            -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" \
            -q 2>&1 | tee -a "$LOG_FILE"; then
            log_success "Keycloak realm 'jeeeraaah-realm' created successfully!"
        else
            log_error "Failed to create Keycloak realm - will retry on backend start"
            log_info "  The realm will be created by Liberty server on first start"
        fi
    else
        log_warn "KeycloakRealmSetup not found - realm will be created on backend start"
    fi
fi

# Final status
log_info "════════════════════════════════════════════════════════════════"
log_success "✅ WSL Startup Complete - All Services Ready!"
log_info "════════════════════════════════════════════════════════════════"
log_info ""
log_info "📊 Container Status:"
docker ps --format "table {{.Names}}\t{{.Status}}" | tee -a "$LOG_FILE"
log_info ""
log_info "📝 Full log: $LOG_FILE"
log_info ""
