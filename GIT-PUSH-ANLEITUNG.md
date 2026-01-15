# Git Push Anleitung

## Änderungen committen und pushen

Führen Sie diese Befehle im Terminal aus:

```bash
cd /home/r-uu/develop/github/main

# 1. Status prüfen
git status

# 2. Alle Änderungen hinzufügen
git add -A

# 3. Commit erstellen
git commit -m "Clean up: Remove JasperReports, consolidate to single InvoiceGenerator with auto page calculation

- Removed JasperReports module (incompatible with Java 25)
- Consolidated InvoiceGenerator and InvoiceGeneratorAdvanced into single class
- InvoiceGenerator now uses automatic page calculation (~24 rows/page)
- Updated all documentation (English + German)
- Cleaned up temporary files and old run configurations
- JPMS fully enabled for docx4j module"

# 4. Push zum Remote
git push
```

## Was wurde geändert?

### Gelöscht:
- Komplettes `jasper/` Modul
- JasperReports Dependencies aus BOM
- Alte `InvoiceGenerator.java` (feste Zeilenzahl)
- Temporäre Dokumentationsdateien
- Alte Run Configurations

### Umbenannt:
- `InvoiceGeneratorAdvanced.java` → `InvoiceGenerator.java`

### Aktualisiert:
- `README.md` Dateien (Englisch + Deutsch)
- `pom.xml` Dateien
- IntelliJ Run Configurations

## Verifikation

Nach dem Push:
```bash
git log --oneline -1
# Sollte zeigen: "Clean up: Remove JasperReports..."
```

