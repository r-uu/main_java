# ✅ IntelliJ Git Push Problem GELÖST

## Was war das Problem?

IntelliJ generiert automatisch askpass-Skripte für Git-Authentifizierung:
- `/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp/intellij-git-askpass-wsl-Ubuntu.sh`

Diese Skripte versuchten, **Windows `java.exe`** aus **WSL** heraus aufzurufen → **Exec format error**

## Was wurde gemacht?

1. ✅ **Fehlerhafte IntelliJ askpass-Skripte ersetzt**
   - Verwenden jetzt `gh auth git-credential` statt Windows java.exe
   - Skripte wurden in `/mnt/c/Users/.../IntelliJIdea2025.3/tmp/` überschrieben

2. ✅ **Git-Konfiguration bereinigt**
   ```bash
   git config --global --unset-all core.askpass
   git config --global credential.https://github.com.helper '!gh auth git-credential'
   ```

3. ✅ **Credentials gesichert**
   - GitHub Token in `~/.git-credentials` gespeichert

4. ✅ **Fix-Skript erstellt**
   - `config/shared/scripts/fix-intellij-git-push.sh`
   - Kann ausgeführt werden, falls IntelliJ die Skripte neu generiert

## Ergebnis

```bash
git push origin main
# ✅ ERFOLG! Push funktioniert!
```

**Git Push funktioniert jetzt wieder problemlos in IntelliJ!**

## Falls das Problem zurückkehrt

IntelliJ könnte die askpass-Skripte neu generieren. Dann einfach ausführen:

```bash
./config/shared/scripts/fix-intellij-git-push.sh
```

Oder automatisch beim Terminal-Start in `~/.bashrc` hinzufügen:
```bash
# Auto-fix IntelliJ Git Push
~/develop/github/main/config/shared/scripts/fix-intellij-git-push.sh 2>/dev/null
```

