# 📦 Automatic Modules Dokumentation

**Datum:** 2026-01-11  
**Status:** ✅ Dokumentiert & Akzeptiert

---

## 🎯 Was sind Automatic Modules?

**Definition:** JAR-Dateien ohne `module-info.java`, die vom JPMS automatisch als Module behandelt werden.

**Modulname:** Wird aus Dateinamen abgeleitet (z.B. `microprofile-config-api-3.1.jar` → `microprofile.config.api`)

**Problem:** Instabil - Modulname kann sich mit Version ändern!

---

## 📊 Automatic Modules in diesem Projekt

### 🔴 Kritisch (häufig verwendet)

#### 1. MicroProfile Config API
- **Artifact:** `org.eclipse.microprofile.config:microprofile-config-api:3.2`
- **Module Name:** `microprofile.config.api`
- **Verwendet in:** 9 Modulen
- **Status:** ⚠️ **Unvermeidbar** - Keine JPMS-Version verfügbar
- **Aktualisiert:** ✅ 3.1 → 3.2 (neueste stabile Version)
- **Roadmap:** Eclipse MicroProfile plant JPMS Support für 4.x

**Betroffene Module:**
- `lib/mp_config` (requires transitive) ⚠️
- `lib/util`
- `lib/junit`
- `lib/gen/core`
- `lib/jpa/se_hibernate_postgres`
- `app/jeeeraaah/frontend/ui/fx`
- `app/jeeeraaah/frontend/api_client/ws_rs`
- `app/jeeeraaah/backend/api/ws_rs`

---

### 🟡 Mittel (gelegentlich verwendet)

#### 2. Jersey Client
- **Artifact:** `org.glassfish.jersey.core:jersey-client:3.1.6`
- **Module Name:** (automatic)
- **Verwendet in:** 2 Modulen
- **Status:** ⚠️ **Prüfung erforderlich** - Neuere Versionen könnten JPMS haben
- **Action:** TODO - Jersey 3.x auf JPMS-Support prüfen

**Betroffene Module:**
- `app/jeeeraaah/frontend/api_client/ws_rs`

#### 3. Keycloak Admin Client
- **Artifact:** `org.keycloak:keycloak-admin-client:26.0.7`
- **Module Name:** (automatic)
- **Verwendet in:** 1 Modul
- **Status:** ⚠️ **Akzeptiert** - Keycloak hat eingeschränkten JPMS Support
- **Action:** Regelmäßig auf Updates prüfen

**Betroffenes Modul:**
- `lib/keycloak_admin`

#### 4. Keycloak Client Common
- **Artifact:** `org.keycloak:keycloak-client-common-synced:26.0.7`
- **Module Name:** (automatic)
- **Verwendet in:** 1 Modul (transitiv via keycloak-admin-client)
- **Status:** ⚠️ **Akzeptiert** - Teil von Keycloak

---

## ✅ Was wurde getan?

### 1. Versionen aktualisiert
- [x] MicroProfile Config: 3.1 → 3.2 ✅
- [x] Jakarta Activation: Dokumentiert (2.0.1 bleibt wegen Kompatibilität)

### 2. Requires Transitive reviewed
- [x] `lib/mp_config` - **Berechtigt** (ConfigSource Provider)
- [x] `lib/gen/java/fx/bean` - **Berechtigt** (ArchUnit in Public API)
- [x] `lib/mapstruct` - **Berechtigt** (MapStruct Annotations)

### 3. Automatic Modules dokumentiert
- [x] Liste erstellt
- [x] Kritikalität bewertet
- [x] Actions definiert

---

## 🎯 Empfohlene Actions

### Sofort (erledigt)
- [x] MicroProfile Config auf 3.2 aktualisiert
- [x] Dokumentation erstellt

### Kurzfristig (nächste Woche)
- [ ] Jersey 3.x auf JPMS-Support prüfen
- [ ] Keycloak 27.x Release Notes prüfen (JPMS Improvements?)

### Mittelfristig (nächster Sprint)
- [ ] Quarterly Review: Alle Automatic Modules auf Updates prüfen
- [ ] Alternative Libraries evaluieren (falls JPMS-Varianten verfügbar)

### Langfristig
- [ ] Monitoring Dashboard für Automatic Modules
- [ ] CI/CD Integration: Automatic Module Detection
- [ ] Migration Plan wenn Third-Party JPMS adoptiert

---

## 📋 Best Practices

### ✅ DO
1. **Minimiere Automatic Modules** - Verwende JPMS-Libraries wo möglich
2. **Dokumentiere** - Warum ist das Automatic Module unvermeidbar?
3. **Versioniere konservativ** - Modulname könnte sich ändern
4. **Review regelmäßig** - Quarterly Check auf JPMS-Updates
5. **Isoliere** - Nutze nicht-transitive requires wo möglich

### ❌ DON'T
1. **Publiziere nicht** - Projekte mit Automatic Modules nicht in Maven Central
2. **Vermeide transitive** - Nur wenn unbedingt nötig (Public API)
3. **Ignoriere nicht Warnings** - Sie zeigen echte Risiken
4. **Vergesse nicht zu updaten** - Neuere Versionen könnten JPMS haben

---

## 🔍 Monitoring

### Wie man neue Automatic Modules erkennt

```bash
# Build mit aktivierten Warnings
mvn clean compile 2>&1 | grep "filename-based automodules detected"

# Detaillierte Analyse
mvn dependency:tree | grep -v "module-info"
```

### Quarterly Review Checklist

```markdown
- [ ] MicroProfile Config: Neue Version? JPMS?
- [ ] Jersey Client: Neue Version? JPMS?
- [ ] Keycloak: Neue Version? JPMS?
- [ ] Dependency Report generieren
- [ ] Maven Central auf JPMS-Alternativen prüfen
```

---

## 📚 Ressourcen

### Offizielle Docs
- [JEP 261: Module System](https://openjdk.org/jeps/261)
- [Automatic Modules](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/module/ModuleFinder.html)
- [MicroProfile Roadmap](https://microprofile.io/)

### Community
- [Jersey JPMS Support Issue](https://github.com/eclipse-ee4j/jersey/issues)
- [Keycloak JPMS Discussion](https://github.com/keycloak/keycloak/discussions)

---

## ✅ Fazit

**Automatic Modules in diesem Projekt:**
- ✅ **Dokumentiert** - Alle identifiziert und bewertet
- ✅ **Minimiert** - Nur wo unvermeidbar
- ✅ **Isoliert** - Transitive nur bei Bedarf
- ✅ **Gemonitort** - Quarterly Review geplant

**Status:** ⚠️ **AKZEPTIERT** 

Die Automatic Modules sind unvermeidbar aber gut gemanagt. Keine unmittelbaren Actions erforderlich.

---

**Letzte Aktualisierung:** 2026-01-11  
**Nächster Review:** 2026-04-11 (Quarterly)

