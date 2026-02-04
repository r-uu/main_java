#!/bin/bash
# Quick Reference - Docker Proxy Commands

echo "════════════════════════════════════════════════════════════════"
echo "  Docker Proxy Configuration - Quick Reference"
echo "════════════════════════════════════════════════════════════════"
echo ""

echo "📋 Proxy-Konfiguration prüfen:"
echo "   sudo systemctl show --property=Environment docker | grep PROXY"
echo ""

echo "🔄 Docker neu starten:"
echo "   sudo systemctl daemon-reload"
echo "   sudo systemctl restart docker"
echo ""

echo "📥 Image pullen (Test):"
echo "   docker pull hello-world"
echo ""

echo "🔨 JasperReports Container bauen:"
echo "   cd /home/r-uu/develop/github/main/config/shared/docker"
echo "   ./docker-compose-build.sh jasperreports"
echo ""

echo "🚀 Container starten:"
echo "   docker compose up -d jasperreports"
echo ""

echo "📊 Status prüfen:"
echo "   docker compose ps"
echo "   docker compose logs -f jasperreports"
echo ""

echo "🏥 Health Check:"
echo "   curl http://localhost:8090/health"
echo ""

echo "════════════════════════════════════════════════════════════════"
