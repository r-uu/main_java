# IntelliJ Maven Troubleshooting

## Aktueller Status (2026-01-12)

Maven Build funktioniert einwandfrei:
- `mvn clean install` auf root: ✅ BUILD SUCCESS
- Alle Dependencies werden korrekt aufgelöst
- Alle Tests laufen durch

## IntelliJ "Unresolved Plugin" Fehler beheben

### Methode 1: Invalidate Caches (Bereits durchgeführt ✅)
1. `File` → `Invalidate Caches...`
2. Wähle `Invalidate and Restart`

### Methode 2: Maven Reimport erzwingen
1. **Maven Tool Window** öffnen (rechte Seite)
2. Klick auf **Reload Button** (🔄 Symbol)
3. Oder: Rechtsklick auf `root/pom.xml` → `Maven` → `Reload Project`

### Methode 3: IntelliJ Maven-Einstellungen
`File` → `Settings` (oder `Ctrl+Alt+S`)
→ `Build, Execution, Deployment` 
→ `Build Tools` 
→ `Maven`

**Wichtige Einstellungen:**
- ✅ Maven home directory: Verwende "Bundled (Maven 3.x)"
- ✅ User settings file: `~/.m2/settings.xml` (falls vorhanden)
- ✅ Local repository: `~/.m2/repository`
- ✅ "Always update snapshots" kann hilfreich sein
- ✅ "Threads" auf mindestens 2 setzen

**Maven Importing:**
- ✅ "Import Maven projects automatically" aktivieren
- ✅ "Automatically download: Sources" aktivieren
- ✅ "Automatically download: Documentation" optional
- ✅ "VM options for importer": `-Xmx2g -XX:MaxMetaspaceSize=512m`

### Methode 4: Projekt-Struktur neu aufbauen
```
1. File → Close Project
2. IntelliJ komplett schließen
3. Lösche folgende Ordner/Dateien (OPTIONAL, nur bei hartnäckigen Problemen):
   - .idea/libraries/
   - .idea/modules/
   - Alle .iml Dateien im Projekt
4. IntelliJ neu starten
5. Projekt öffnen ("Open or Import")
6. Warte bis Auto-Import abgeschlossen ist
```

### Methode 5: Maven Cache bereinigen (Kommandozeile)
```bash
# Im WSL Terminal:
cd /home/r-uu/develop/github/main/root

# Dependencies neu auflösen
mvn dependency:resolve -U

# Bei Bedarf: Lokale Repository-Probleme beheben
rm -rf ~/.m2/repository/org/apache/maven/plugins
mvn clean install -U
```

### Methode 6: IntelliJ Build-Delegation prüfen
`File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`

**Empfohlen:**
- ✅ "Delegate IDE build/run actions to Maven" **aktivieren**
  - ✅ Build project
  - ✅ Run tests
  
Dies stellt sicher, dass IntelliJ Maven für alle Build-Aktivitäten verwendet.

## Häufige Ursachen für "Unresolved Plugin" Warnings

### 1. Plugin ohne Version im Child POM
**Problem:** Plugin wird in `root/pom.xml` verwendet, aber Version ist nur in `bom/pom.xml` definiert.

**Lösung:** IntelliJ kann manchmal die Vererbung nicht korrekt auflösen. Dies ist ein bekanntes IntelliJ-Problem und bedeutet NICHT, dass der Build fehlerhaft ist.

**Workaround:** 
- Ignorieren (Build funktioniert)
- Oder: Plugin-Version explizit im `root/pom.xml` angeben (nicht empfohlen, da Duplikation)

### 2. BOM Import nicht vollständig aufgelöst
**Problem:** Dependencies aus `<dependencyManagement>` mit `<scope>import</scope>` werden nicht erkannt.

**Lösung:** 
1. Maven Reimport (Methode 2)
2. Bei Bedarf: IntelliJ Update auf neueste Version

### 3. Multi-Modul Projekt Komplexität
**Problem:** Bei großen Multi-Modul-Projekten kann IntelliJ manchmal die Abhängigkeiten zwischen Modulen nicht sofort auflösen.

**Lösung:**
1. Warte bis Indexierung abgeschlossen ist (Status-Bar unten rechts)
2. Maven Reimport nach Indexierung

## Verifikation

### Build erfolgreich?
```bash
cd /home/r-uu/develop/github/main/root
mvn clean install
```
✅ Erwartung: `BUILD SUCCESS`

### Alle Module kompilieren?
```bash
cd /home/r-uu/develop/github/main/root
mvn compile
```
✅ Erwartung: Keine Compilation Errors

### IntelliJ Build erfolgreich?
1. `Build` → `Rebuild Project`
2. ✅ Erwartung: "Build completed successfully"

## Bekannte Nicht-kritische Warnings

Folgende Warnings in IntelliJ können ignoriert werden, wenn Maven Build funktioniert:

- ⚠️ "Unresolved plugin: 'org.apache.maven.plugins:maven-compiler-plugin'"
  → Version kommt aus BOM, Maven löst korrekt auf
  
- ⚠️ "Unresolved plugin: 'org.apache.maven.plugins:maven-surefire-plugin'"
  → Version kommt aus BOM, Maven löst korrekt auf

## Kritische Fehler (müssen behoben werden)

- ❌ Rote Unterstreichungen in Java-Code
- ❌ "Cannot resolve symbol" für Java-Klassen
- ❌ Compilation Errors beim IntelliJ Build
- ❌ Maven Build schlägt fehl

## Weitere Hilfe

Wenn alle oben genannten Schritte nicht helfen:

1. **IntelliJ Logs prüfen:**
   - `Help` → `Show Log in Explorer/Finder`
   - Nach "ERROR" oder "Exception" suchen
   
2. **IntelliJ Version prüfen:**
   - `Help` → `About`
   - Mindestens IntelliJ IDEA 2024.x empfohlen für Java 25 Support
   
3. **Maven-Kompatibilität:**
   - Aktuelles Projekt verwendet Maven 3.9.x
   - IntelliJ bundled Maven sollte kompatibel sein

## Zusammenfassung

**Status:** ✅ Projekt ist funktionsfähig
- Maven Build: ✅ Erfolgreich
- Tests: ✅ Laufen durch
- IntelliJ Warnings: ⚠️ Kosmetisch (können meist ignoriert werden)

**Empfohlene Aktion:**
1. Invalidate Caches (bereits gemacht ✅)
2. Maven Reload (Methode 2)
3. Falls Warnings bleiben: Ignorieren, solange Build funktioniert

