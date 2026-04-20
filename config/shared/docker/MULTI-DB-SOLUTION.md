# 🎯 PostgreSQL Multi-Database Setup - Die elegante Lösung

**Problem:** Wir brauchen 2 Datenbanken: `jeeeraaah` und `lib_test`  
**PostgreSQL-Limitierung:** Nur EINE Datenbank via `POSTGRES_DB` Umgebungsvariable  
**Lösung:** **Entrypoint-Wrapper** - Best Practice für Multi-DB-Setup!

---

## ❌ Warum nicht zwei Umgebungsvariablen?

**Das war deine Frage:**
> "warum definieren wir nicht eine zweite umgebungsvariable analog zu postgres_db?"

**Antwort:**

PostgreSQL's offizielles Docker-Image unterstützt nur diese Standard-Variablen:
- `POSTGRES_DB` - **Eine** Datenbank (Standard: postgres)
- `POSTGRES_USER` - Ein User
- `POSTGRES_PASSWORD` - Ein Passwort

**Es gibt kein:**
- ❌ `POSTGRES_DB_ADDITIONAL`
- ❌ `POSTGRES_DBS` (Liste)
- ❌ `POSTGRES_DB_2`

Das Image ist nicht dafür designed, mehrere Datenbanken aus Env-Vars zu erstellen.

---

## ✅ Die beste Lösung: Entrypoint-Wrapper

**Das ist die Standard-Lösung in der Docker-Community!**

### **Wie es funktioniert:**

1. **Entrypoint-Wrapper-Skript** wird gestartet
2. Wrapper startet den originalen PostgreSQL-Entrypoint
3. **Im Hintergrund:** Wartet bis PostgreSQL bereit ist
4. **Dann:** Erstellt alle konfigurierten Datenbanken
5. **Fertig!** Beide Datenbanken existieren

### **Vorteile:**

✅ **Einheitlich:** Beide Datenbanken werden auf dem **gleichen Weg** erstellt  
✅ **Elegant:** Ein Skript, eine klare Verantwortung  
✅ **Verständlich:** Alle Datenbanken an **einem Ort** konfiguriert  
✅ **Erweiterbar:** Neue Datenbank? Einfach zur Liste hinzufügen!  
✅ **Standard:** Best Practice in der Docker-Community  

---

## 📝 Die Implementierung

### **1. Entrypoint-Wrapper konfiguriert alle Datenbanken:**

**Datei:** `config/shared/docker/postgres-entrypoint-wrapper.sh`

```bash
# Alle benötigten Datenbanken - ein zentraler Ort!
DATABASES=(
    "jeeeraaah:r_uu:Main application database"
    "lib_test:r_uu:Library test database"
)

# Funktion erstellt alle Datenbanken nach PostgreSQL-Start
ensure_databases() {
    for db_config in "${DATABASES[@]}"; do
        # Erstelle Datenbank falls nicht vorhanden
        # Idempotent: Kann mehrfach ausgeführt werden
    done
}
```

**Neue Datenbank hinzufügen?** Einfach zur Liste hinzufügen:
```bash
DATABASES=(
    "jeeeraaah:r_uu:Main application database"
    "lib_test:r_uu:Library test database"
    "analytics:r_uu:Analytics database"  # ← Neu!
)
```

---

### **2. Docker Compose verwendet den Wrapper:**

**Datei:** `config/shared/docker/docker-compose.yml`

```yaml
postgres-jeeeraaah:
  image: postgres:16-alpine
  entrypoint: ["/bin/bash", "/usr/local/bin/postgres-entrypoint-wrapper.sh"]
  command: ["postgres"]
  volumes:
    - ./postgres-entrypoint-wrapper.sh:/usr/local/bin/postgres-entrypoint-wrapper.sh:ro
```

---

### **3. Healthcheck prüft nur:**

**Datei:** `config/shared/docker/healthcheck/postgres-healthcheck.sh`

```bash
# Healthcheck hat EINE Aufgabe: Prüfen!
# Datenbank-Erstellung ist NICHT seine Aufgabe!

pg_isready -U r_uu || exit 1

# Prüfe ob alle benötigten Datenbanken existieren
for db in jeeeraaah lib_test; do
    # Falls fehlt: exit 1 (unhealthy)
done
```

**Separation of Concerns:**
- **Entrypoint:** Erstellt Datenbanken
- **Healthcheck:** Prüft Gesundheit

---

## 🔄 Ablauf beim Container-Start

```
1. Docker startet Container
   ↓
2. Entrypoint-Wrapper startet
   ↓
3. Wrapper startet PostgreSQL (im Vordergrund)
   ↓
4. Wrapper startet Setup-Funktion (im Hintergrund)
   ↓
5. Setup wartet bis PostgreSQL bereit ist
   ↓
6. Setup erstellt jeeeraaah (falls nicht vorhanden)
   ↓
7. Setup erstellt lib_test (falls nicht vorhanden)
   ↓
8. Healthcheck prüft: Beide Datenbanken da? ✅
   ↓
9. Container ist HEALTHY
```

---

## 🆚 Vergleich: Vorher vs. Nachher

### **Vorher (komplex):**

- ❌ `jeeeraaah` via `POSTGRES_DB` (Umgebungsvariable)
- ❌ `lib_test` via Healthcheck (erstellt bei jedem Check)
- ❌ `lib_test` via Init-Skript (nur beim ersten Start)
- ❌ Drei verschiedene Mechanismen!
- ❌ Schwer zu verstehen
- ❌ Schwer zu erweitern

### **Nachher (elegant):**

- ✅ **Beide** via Entrypoint-Wrapper
- ✅ **Ein** Mechanismus für alle Datenbanken
- ✅ **Ein** zentraler Ort für die Konfiguration
- ✅ Einfach zu verstehen
- ✅ Einfach zu erweitern

---

## 💡 Warum ist das "Best Practice"?

### **1. Single Responsibility Principle:**
- **Entrypoint:** Setup (Datenbanken erstellen)
- **Healthcheck:** Monitoring (Status prüfen)
- **PostgreSQL:** Datenbank-Server

### **2. Idempotenz:**
Der Wrapper kann mehrfach ausgeführt werden:
```bash
# Prüft immer erst: Existiert DB schon?
if psql -lqt | grep -qw "$dbname"; then
    echo "✅ Existiert bereits"
else
    echo "→ Erstelle..."
fi
```

### **3. Zentrale Konfiguration:**
Alle Datenbanken an einem Ort:
```bash
DATABASES=(
    "db1:owner:description"
    "db2:owner:description"
    # Hier neue DBs hinzufügen!
)
```

### **4. Lesbar & Wartbar:**
Jeder versteht sofort: "Ah, hier werden die Datenbanken konfiguriert!"

---

## 🎓 Weitere Möglichkeiten (nicht gewählt)

### **Option 1: Init-Skript in `/docker-entrypoint-initdb.d/`**

❌ **Problem:** Läuft nur beim **ersten** Start (wenn Volume leer ist)  
❌ Bei bestehendem Volume → wird übersprungen

### **Option 2: Healthcheck erstellt Datenbanken**

❌ **Problem:** Healthcheck sollte nur **prüfen**, nicht **ändern**  
❌ Verletzt Single Responsibility Principle

### **Option 3: Zwei separate PostgreSQL-Container**

❌ **Problem:** Ressourcen-Verschwendung  
❌ Komplexere Konfiguration

### **✅ Option 4: Entrypoint-Wrapper (gewählt)**

✅ Standard-Lösung in der Docker-Community  
✅ Flexibel, erweiterbar, verständlich  
✅ Funktioniert mit allen Volumes (neu & bestehend)

---

## 📊 Zusammenfassung

| Aspekt | Umgebungsvariable | Entrypoint-Wrapper |
|--------|-------------------|-------------------|
| **Mehrere DBs** | ❌ Nicht unterstützt | ✅ Beliebig viele |
| **Zentrale Config** | ❌ Verteilt | ✅ Ein Ort |
| **Erweiterbar** | ❌ Schwierig | ✅ Einfach |
| **Verständlich** | ❌ Unterschiedlich | ✅ Einheitlich |
| **Best Practice** | ❌ Nein | ✅ Ja |

---

## ✅ Deine Frage beantwortet

> "können wir das alles nicht einheitlich, elegant, leicht verständlich handhaben?"

**JA! Genau das haben wir jetzt:**

1. ✅ **Einheitlich:** Beide Datenbanken via Entrypoint-Wrapper
2. ✅ **Elegant:** Ein Skript, eine klare Verantwortung
3. ✅ **Leicht verständlich:** Konfiguration an einem Ort
4. ✅ **Erweiterbar:** Neue DB? Eine Zeile hinzufügen!

---

## 🚀 Nächste Schritte

**Die neue Lösung ist bereits implementiert!**

```bash
# Container mit neuer Lösung starten
cd /home/r-uu/develop/github/java/main/config/shared/docker
docker compose down
docker compose up -d

# Testen
ruu-test
```

**Fertig!** Beide Datenbanken werden jetzt **einheitlich** und **elegant** erstellt! ✨
