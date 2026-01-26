#!/bin/bash
# Quick reset and diagnostic script

LOG_FILE="/tmp/docker-reset-$(date +%Y%m%d-%H%M%S).log"

echo "Starting Docker reset and diagnostic..." | tee "$LOG_FILE"
echo "Log file: $LOG_FILE" | tee -a "$LOG_FILE"
echo ""

# Stop and remove everything
echo "1. Stopping all containers..." | tee -a "$LOG_FILE"
docker compose down -v >> "$LOG_FILE" 2>&1

echo "2. Removing orphan containers..." | tee -a "$LOG_FILE"
docker ps -a | grep -E "keycloak|postgres|jasper" | awk '{print $1}' | xargs -r docker rm -f >> "$LOG_FILE" 2>&1

echo "3. Starting fresh containers..." | tee -a "$LOG_FILE"
docker compose up -d >> "$LOG_FILE" 2>&1

echo "4. Waiting 10 seconds for initialization..." | tee -a "$LOG_FILE"
sleep 10

echo "5. Container status:" | tee -a "$LOG_FILE"
docker ps >> "$LOG_FILE" 2>&1
docker ps

echo "" | tee -a "$LOG_FILE"
echo "6. Keycloak logs (last 30 lines):" | tee -a "$LOG_FILE"
docker logs keycloak --tail 30 >> "$LOG_FILE" 2>&1

echo "" | tee -a "$LOG_FILE"
echo "7. PostgreSQL jeeeraaah logs (last 20 lines):" | tee -a "$LOG_FILE"
docker logs postgres-jeeeraaah --tail 20 >> "$LOG_FILE" 2>&1

echo "" | tee -a "$LOG_FILE"
echo "✅ Diagnostic complete. Check log: $LOG_FILE" | tee -a "$LOG_FILE"
cat "$LOG_FILE"
