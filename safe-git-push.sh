#!/bin/bash
# Safe Git Push - Commits nur sichere Änderungen

echo "════════════════════════════════════════════════════════════════"
echo "  Safe Git Push Helper"
echo "════════════════════════════════════════════════════════════════"
echo ""

cd /home/r-uu/develop/github/main

echo "📋 Aktueller Status:"
git status --short
echo ""

echo "⚠️  Dateien die NICHT committed werden sollten:"
echo "   - config/shared/docker/.env (enthält lokale Proxy-Credentials)"
echo ""

read -p "Möchten Sie die sicheren Änderungen committen? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "🔄 Erstelle Commit..."

    git commit -m "Add Docker proxy support and environment configuration

- Add .env to .gitignore to prevent committing sensitive proxy credentials
- Add .env.example as template for environment configuration
- Add optional HTTP_PROXY/HTTPS_PROXY build args to JasperReports Dockerfile
- Use HTTP instead of HTTPS for Alpine repositories (proxy compatibility workaround)
- Add comprehensive proxy configuration documentation (PROXY-*.md)
- Add helper scripts for proxy setup and container builds
- Update docker-compose.yml to pass optional proxy build args
- Add Git push safety analysis documentation

Changes are backward compatible:
- Proxy configuration is optional and only used when env vars are set
- HTTP Alpine repositories work with and without proxy
- Default behavior unchanged for systems without proxy

Safe for Windows/WSL2 systems - no breaking changes to existing setups."

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Commit erfolgreich!"
        echo ""
        read -p "Möchten Sie jetzt pushen? (y/n) " -n 1 -r
        echo ""

        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo ""
            echo "🚀 Pushing to GitHub..."
            git push

            if [ $? -eq 0 ]; then
                echo ""
                echo "✅ Push erfolgreich!"
                echo ""
                echo "📝 Hinweis für Windows/WSL2 System:"
                echo "   Nach dem Pull auf dem anderen System:"
                echo "   1. .env.example nach .env kopieren"
                echo "   2. Proxy-Einstellungen anpassen (falls benötigt)"
                echo "   3. docker compose up -d"
            else
                echo ""
                echo "❌ Push fehlgeschlagen!"
            fi
        else
            echo ""
            echo "ℹ️  Push abgebrochen. Sie können später pushen mit:"
            echo "   git push"
        fi
    else
        echo ""
        echo "❌ Commit fehlgeschlagen!"
    fi
else
    echo ""
    echo "ℹ️  Abgebrochen. Keine Änderungen committed."
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
