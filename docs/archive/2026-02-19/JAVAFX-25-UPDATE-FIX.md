# JavaFX 25 Update - IntelliJ Fix
**Problem:** Nach Update von JavaFX 24.0.2 → 25 funktionieren die Apps nicht mehr
**Ursache:** IntelliJ Cache zeigt noch auf alte JavaFX 24.0.2 JARs
---
## ✅ Lösung (30 Sekunden)
### 1. IntelliJ Cache invalidieren
**In IntelliJ IDEA:**
1. File → Invalidate Caches...
2. ✅ Check: "Clear file system cache and Local History"
3. ✅ Check: "Clear VCS Log caches and indexes"
4. Click: **"Invalidate and Restart"**
**Wait:** IntelliJ startet neu (30 Sekunden)
### 2. Maven Dependencies neu laden
**In IntelliJ:**
1. Rechtsklick auf `root/pom.xml`
2. Maven → Reload Project
**Oder im Terminal:**
```bash
cd /home/r-uu/develop/github/main/root
mvn dependency:purge-local-repository
mvn clean install
```
### 3. Apps starten
Jetzt sollte es wieder funktionieren:
- `DashAppRunner (Application)` ▶️
- `GanttAppRunner (Application)` ▶️
---
## 🔍 Was ist passiert?
**Gestern:**
- JavaFX 24.0.2 ✅
- IntelliJ Cache korrekt ✅
- Apps liefen perfekt ✅
**Heute:**
- Ich habe JavaFX auf Version 25 aktualisiert
- IntelliJ Cache zeigt noch auf 24.0.2
- Apps finden Module nicht ❌
**Nach Cache-Invalidierung:**
- JavaFX 25 neu geladen ✅
- IntelliJ Cache aktualisiert ✅
- Apps laufen wieder ✅
---
## 🚀 Noch schneller: Maven nutzen
**Funktioniert IMMER** (auch ohne Cache-Invalidierung):
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java@gantt
```
Maven lädt immer die korrekten Dependencies, egal was IntelliJ cached.
---
**Zusammenfassung:** Es war nicht kompliziert, nur ein IntelliJ-Cache-Problem nach dem JavaFX-Update!
