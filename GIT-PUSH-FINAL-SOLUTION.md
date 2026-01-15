# Git Push in IntelliJ (WSL)

## Problem
IntelliJ ruft Git mit `-c credential.helper=` auf, was alle Credential Helper deaktiviert.
Das IntelliJ askpass-Skript versucht Windows `java.exe` aus WSL aufzurufen → "Exec format error"

## Lösung

**Token direkt in die Remote-URL einbetten:**

```bash
# Token holen
TOKEN=$(gh auth status -t 2>&1 | grep -oP 'Token: \K.*')

# Remote-URL mit Token setzen
git remote set-url origin https://r-uu:${TOKEN}@github.com/r-uu/main.git
```

✅ Funktioniert zuverlässig - Git braucht keinen Credential Helper mehr

## Token erneuern (bei Ablauf)

```bash
gh auth refresh
TOKEN=$(gh auth status -t 2>&1 | grep -oP 'Token: \K.*')
git remote set-url origin https://r-uu:${TOKEN}@github.com/r-uu/main.git
```

## Sicherheit

- Token ist in `.git/config` (lokal, nicht in Git)
- Nur für deinen User lesbar
- Standard-Praxis für Token-Auth

