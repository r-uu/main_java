#!/bin/bash
# fix-intellij-wsl-rendering.sh v4
# Behebt IntelliJ-Rendering-Probleme unter WSL/WSLg UND Windows-nativ:
#   - Grauer Schleier (grey veil)  → sun.java2d.pmoffscreen + xrender aus
#   - Zu groß / niedrige Auflösung → sun.java2d.uiScale=0.75
#   - Schwarze/weiße Bereiche       → opengl + pmoffscreen aus
#   - Flackern / Tearing            → vsync aus
#   - Font-Antialiasing             → gasp (fontgesteuert)
#   - JCEF (Copilot-Panel)          → Offscreen-Rendering
#   - UTF-8 Encoding                → file.encoding + stdout.encoding
#
# Patcht BEIDE vmoptions-Dateien:
#   - WSL:     ~/.config/JetBrains/IntelliJIdea*/idea64.vmoptions
#   - Windows: /mnt/c/Users/r-uu/AppData/Roaming/JetBrains/IntelliJIdea*/idea64.exe.vmoptions

# ─── Konfiguration ────────────────────────────────────────────────────────────
# UI-Skalierung: 0.75 = verkleinert (war zu groß bei 100% Windows-DPI und WSLg)
# Anpassen falls zu klein: UI_SCALE=0.85 ruu-ij-fix
UI_SCALE=${UI_SCALE:-0.75}

# ─── Hilfsfunktion: WSL vmoptions patchen ────────────────────────────────────
patch_wsl_vmoptions() {
    local VMOPTIONS="$1"
    echo "📝 [WSL]     $VMOPTIONS"

    # Alten Block + einzelne Flags entfernen
    sed -i "/^# WSL\/WSLg Rendering Fixes/,/^-Dstdout\.encoding=UTF-8$/d" "$VMOPTIONS"
    sed -i "/^# WSL\/WSLg Rendering Fixes/,/^-Dsun\.java2d\.vsync=false$/d" "$VMOPTIONS"
    for FLAG in \
        "sun.java2d.opengl" \
        "sun.java2d.xrender" \
        "sun.java2d.pmoffscreen" \
        "sun.java2d.uiScale" \
        "sun.java2d.vsync" \
        "awt.useSystemAAFontSettings" \
        "swing.aatext" \
        "jcef.forceOffscreenRendering" \
        "file.encoding" \
        "stdout.encoding"
    do
        sed -i "/^-D${FLAG}=/d" "$VMOPTIONS"
    done

    cat >> "$VMOPTIONS" << EOF

# WSL/WSLg Rendering Fixes v4 (by fix-intellij-wsl-rendering.sh)
-Dsun.java2d.opengl=false
-Dsun.java2d.xrender=false
-Dsun.java2d.pmoffscreen=false
-Dsun.java2d.uiScale=${UI_SCALE}
-Dawt.useSystemAAFontSettings=gasp
-Dswing.aatext=true
-Dsun.java2d.vsync=false
-Djcef.forceOffscreenRendering=true
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
EOF
    echo "   ✅ WSL-Flags gesetzt (uiScale=${UI_SCALE})"
}

# ─── Hilfsfunktion: Windows vmoptions patchen ────────────────────────────────
patch_windows_vmoptions() {
    local VMOPTIONS="$1"
    echo "📝 [Windows] $VMOPTIONS"

    # Alten Block + einzelne Flags entfernen
    sed -i "/^# Windows Display Scaling/,/^-Dstdout\.encoding=UTF-8$/d" "$VMOPTIONS"
    for FLAG in \
        "sun.java2d.uiScale" \
        "file.encoding" \
        "stdout.encoding"
    do
        sed -i "/^-D${FLAG}=/d" "$VMOPTIONS"
    done

    # Sicherstellen dass Datei mit Newline endet
    echo "" >> "$VMOPTIONS"

    cat >> "$VMOPTIONS" << EOF

# Windows Display Scaling Fix v4 (by fix-intellij-wsl-rendering.sh)
-Dsun.java2d.uiScale=${UI_SCALE}
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
EOF
    echo "   ✅ Windows-Flags gesetzt (uiScale=${UI_SCALE})"
}

# ─── WSL vmoptions finden und patchen ────────────────────────────────────────
echo ""
echo "═══ WSL vmoptions ═══════════════════════════════════════════════════════"
WSL_VMOPTIONS_FOUND=0
while IFS= read -r -d '' VMOPTS; do
    patch_wsl_vmoptions "$VMOPTS"
    WSL_VMOPTIONS_FOUND=1
done < <(find "$HOME/.config/JetBrains" -name "idea64.vmoptions" -print0 2>/dev/null | sort -z)
if [ "$WSL_VMOPTIONS_FOUND" -eq 0 ]; then
    echo "⚠️  Keine WSL idea64.vmoptions gefunden (IntelliJ einmal starten)"
fi

# ─── Windows vmoptions finden und patchen ────────────────────────────────────
echo ""
echo "═══ Windows vmoptions ═══════════════════════════════════════════════════"
WIN_USER_HOME="/mnt/c/Users/r-uu"
WIN_VMOPTIONS_FOUND=0
while IFS= read -r -d '' VMOPTS; do
    patch_windows_vmoptions "$VMOPTS"
    WIN_VMOPTIONS_FOUND=1
done < <(find "${WIN_USER_HOME}/AppData/Roaming/JetBrains" -name "idea64.exe.vmoptions" -print0 2>/dev/null | sort -z)
if [ "$WIN_VMOPTIONS_FOUND" -eq 0 ]; then
    echo "⚠️  Keine Windows idea64.exe.vmoptions gefunden"
fi

# ─── Zusammenfassung ─────────────────────────────────────────────────────────
echo ""
echo "════════════════════════════════════════════════════════════════════════"
echo "✅ IntelliJ-Rendering-Fixes v4 angewendet"
echo ""
echo "   uiScale=${UI_SCALE}   ← bei zu groß: kleiner; bei zu klein: größer"
echo ""
echo "   WSL-Fixes (nur Linux):"
echo "     sun.java2d.opengl=false, xrender=false, pmoffscreen=false  ← Schleier"
echo "     vsync=false, jcef.forceOffscreenRendering=true"
echo ""
echo "   Beide Plattformen:"
echo "     sun.java2d.uiScale=${UI_SCALE}, awt.useSystemAAFontSettings=gasp"
echo "     file.encoding=UTF-8"
echo ""
echo "💡 Skalierung anpassen:"
echo "   UI_SCALE=0.85 ruu-ij-fix   → etwas größer"
echo "   UI_SCALE=0.65 ruu-ij-fix   → noch kleiner"
echo ""
echo "🔄 IntelliJ neu starten, damit die Änderungen wirksam werden."
echo "════════════════════════════════════════════════════════════════════════"
