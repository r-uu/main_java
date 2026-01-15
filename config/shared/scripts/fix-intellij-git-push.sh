#!/bin/bash
# Permanenter Fix für IntelliJ Git Push in WSL
# Ersetzt die fehlerhaften IntelliJ askpass-Skripte, die Windows java.exe aufrufen

INTELLIJ_TMP="/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp"

echo "=== IntelliJ Git Push Fix für WSL ==="
echo ""

# Erstelle funktionierende askpass-Skripte
cat > "$INTELLIJ_TMP/intellij-git-askpass-wsl-Ubuntu.sh" << 'EOF'
#!/bin/sh
# Fixed askpass for IntelliJ in WSL - uses gh CLI instead of broken Windows java.exe
exec gh auth git-credential "$@"
EOF

cat > "$INTELLIJ_TMP/intellij-ssh-askpass-wsl-Ubuntu.sh" << 'EOF'
#!/bin/sh
# Fixed SSH askpass for IntelliJ in WSL
exec gh auth git-credential "$@"
EOF

# Setze Berechtigungen
chmod +x "$INTELLIJ_TMP/intellij-git-askpass-wsl-Ubuntu.sh"
chmod +x "$INTELLIJ_TMP/intellij-ssh-askpass-wsl-Ubuntu.sh"

echo "✅ IntelliJ askpass-Skripte wurden ersetzt"
echo ""
echo "Die Skripte verwenden jetzt 'gh auth git-credential' statt Windows java.exe"
echo ""
echo "Git Push sollte jetzt in IntelliJ funktionieren!"

