# ✅ PostgreSQL Multi-Database Setup - Elegante Lösung

## Problem

Wir brauchen 2 Datenbanken: `jeeeraaah` und `lib_test`

**PostgreSQL-Limitierung:** Nur EINE Datenbank via `POSTGRES_DB` Umgebungsvariable

---

## Lösung: Entrypoint-Wrapper (Best Practice!)

### **Warum nicht zwei Umgebungsvariablen?**

PostgreSQL's Docker-Image unterstützt nur:
- `POSTGRES_DB` - **Eine** Datenbank
- Kein `POSTGRES_DB_2` oder `POSTGRES_DBS`

**Das ist Standard!** Alle Projekte mit mehreren Datenbanken nutzen einen **Entrypoint-Wrapper**.

---

### **Die elegante Lösung:**

**Datei:** `postgres-entrypoint-wrapper.sh`

```bash
# Zentrale Konfiguration - ALLE Datenbanken an einem Ort!
DATABASES=(
    "jeeeraaah:r_uu:Main application database"
    "lib_test:r_uu:Library test database"
)

# Erstellt alle Datenbanken nach PostgreSQL-Start
ensure_databases() {
    for db_config in "${DATABASES[@]}"; do
        # Prüft: Existiert schon? → Skip
        # Nicht vorhanden? → Erstellen
    done
}
```

**Beide Datenbanken auf dem GLEICHEN Weg!** ✅

---

## Wie es funktioniert

```
1. Docker startet Container
   ↓
2. Entrypoint-Wrapper startet
   ↓
3. Wrapper startet PostgreSQL
   ↓
4. Im Hintergrund: Wartet bis PostgreSQL bereit
   ↓
5. Erstellt jeeeraaah (falls nicht vorhanden)
   ↓
6. Erstellt lib_test (falls nicht vorhanden)
   ↓
7. ✅ Beide Datenbanken da!
```

---

## Vorteile

✅ **Einheitlich:** Beide Datenbanken via Entrypoint-Wrapper  
✅ **Elegant:** Ein Skript, eine Verantwortung  
✅ **Verständlich:** Konfiguration an einem Ort  
✅ **Erweiterbar:** Neue DB? Eine Zeile hinzufügen!  
✅ **Best Practice:** Standard in der Docker-Community  

---

## Vergleich: Alt vs. Neu

### **Alt (komplex):**
- ❌ `jeeeraaah` via Env-Var
- ❌ `lib_test` via Healthcheck
- ❌ Zwei verschiedene Wege!

### **Neu (elegant):**
- ✅ **Beide** via Entrypoint-Wrapper
- ✅ **Ein** Weg für alle Datenbanken!

---

## Verifikation

```bash
# Container neu starten
docker compose down && docker compose up -d

# Logs beobachten
docker logs -f postgres-jeeeraaah
```

**Erwartete Ausgabe:**
```
📦 PostgreSQL Entrypoint Wrapper gestartet
→ Starte PostgreSQL Server...
→ Prüfe und erstelle Datenbanken...
✅ PostgreSQL ist bereit
→ Prüfe Datenbank: jeeeraaah
  ✅ jeeeraaah erstellt (Main application database)
→ Prüfe Datenbank: lib_test
  ✅ lib_test erstellt (Library test database)
✅ Alle Datenbanken bereit!
```

**Datenbanken prüfen:**
```bash
docker exec postgres-jeeeraaah psql -U r_uu -d postgres -c "\l"
```

**Muss enthalten:**
- ✅ `jeeeraaah`
- ✅ `lib_test`

---

## Zusammenfassung

**Frage:** Warum nicht zwei Umgebungsvariablen?

**Antwort:** PostgreSQL unterstützt nur EINE DB via Env-Var

**Lösung:** Entrypoint-Wrapper - **Best Practice** für Multi-DB-Setup!

**Ergebnis:** Beide Datenbanken werden **einheitlich** und **elegant** erstellt! ✅

**Mehr Details:** `MULTI-DB-SOLUTION.md`
