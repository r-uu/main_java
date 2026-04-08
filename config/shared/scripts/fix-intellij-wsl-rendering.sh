#!/bin/bash
# fix-intellij-wsl-rendering.sh v2
# Behebt IntelliJ-Rendering-Probleme unter WSL/WSLg
# - XRender aus (Artefakte bei uiScale != 1.0 mit JBR 21+)
# - JCEF Offscreen-Rendering (Copilot-Panel, Browser-Plugins)
# - Font-Antialiasing: gasp (fontgesteuert, kein hartes lcd-Forcing)
# - UTF-8 Encoding sicherstellen

VMOPTIONS="$HOME/.config/JetBrains/IntelliJIdea2026.1/idea64.vmoptions"

if [ ! -f "$VMOPTIONS" ]; then
    echo "❌ Datei nicht gefunden: $VMOPTIONS"
    echo "   Suche nach anderen Versionen..."
    VMOPTIONS=$(find "$HOME/.config/JetBrains" -name "idea64.vmoptions" 2>/dev/null | head -1)
    if [ -z "$VMOPTIONS" ]; then
        echo "❌ Keine idea64.vmoptions gefunden. IntelliJ einmal starten, dann erneut ausführen."
        exit 1
    fi
    echo "✅ Gefunden: $VMOPTIONS"
fi

MARKER="# WSL/WSLg Rendering Fixes"

if grep -q "$MARKER" "$VMOPTIONS" 2>/dev/null; then
    echo "⚠️  WSL-Rendering-Fixes bereits vorhanden. Ersetze mit v2-Settings..."
    # Alten Block entfernen (von MARKER bis zum letzten gesetzten Flag)
    sed -i "/^# WSL\/WSLg Rendering Fixes/,/^-Dstdout\.encoding=UTF-8$/d" "$VMOPTIONS"
    # Auch alten v1-Block ohne stdout.encoding entfernen
    sed -i "/^# WSL\/WSLg Rendering Fixes/,/^-Dsun\.java2d\.vsync=false$/d" "$VMOPTIONS"
fi

cat >> "$VMOPTIONS" << 'EOF'

# WSL/WSLg Rendering Fixes v2 (by fix-intellij-wsl-rendering.sh)
# OpenGL in WSL unzuverlässig → deaktivieren
-Dsun.java2d.opengl=false
# XRender mit uiScale=1.5 + JBR 21 erzeugt Linien-Artefakte in Menüs → aus
-Dsun.java2d.xrender=false
# Pixmap-Offscreen-Buffer aus (verhindert schwarze/weiße Bereiche)
-Dsun.java2d.pmoffscreen=false
# Font-Antialiasing: gasp = fontgesteuert (kein hartes lcd-Forcing)
-Dawt.useSystemAAFontSettings=gasp
-Dswing.aatext=true
# VSync aus (verhindert Flackern/Tearing unter WSLg)
-Dsun.java2d.vsync=false
# JCEF (Copilot-Panel, Browser-Plugins) → Offscreen-Rendering für WSLg
-Djcef.forceOffscreenRendering=true
# Encoding: verhindert Zeichensatz-Artefakte
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
EOF

echo ""
echo "✅ WSL-Rendering-Fixes v2 wurden gesetzt in:"
echo "   $VMOPTIONS"
echo ""
echo "📋 Gesetzte Optionen:"
echo "   -Dsun.java2d.opengl=false          (OpenGL deaktiviert)"
echo "   -Dsun.java2d.xrender=false         (XRender aus → keine Linien-Artefakte)"
echo "   -Dsun.java2d.pmoffscreen=false     (keine Pixmap-Artefakte)"
echo "   -Dawt.useSystemAAFontSettings=gasp (fontgesteuertes AA)"
echo "   -Dswing.aatext=true                (Swing-Antialiasing)"
echo "   -Dsun.java2d.vsync=false           (kein Flackern)"
echo "   -Djcef.forceOffscreenRendering=true (Copilot-Panel-Fix)"
echo "   -Dfile.encoding=UTF-8              (Zeichensatz-Fix)"
echo "   -Dstdout.encoding=UTF-8            (Zeichensatz-Fix)"
echo ""
echo "🔄 Bitte IntelliJ neu starten, damit die Änderungen wirksam werden."

