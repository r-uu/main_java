# 🔧 Resource Management Fix - AutoCloseable & try-with-resources
**Datum:** 2026-02-26  
**Status:** ✅ Erfolgreich behoben
---
## 📋 Problem
EntityManager und EntityManagerFactory implementieren `AutoCloseable` und müssen korrekt geschlossen werden, um Resource Leaks zu vermeiden.
**Warnung:**
```
Resource leak: 'entityManager' is not closed
Resource leak: 'entityManagerFactory' is not closed
```
---
## 🔄 Durchgeführte Fixes
### 1. EntityManagerFactoryProducerTest - try-with-resources
**Datei:** `lib/jpa/se.hibernate/src/test/java/.../EntityManagerFactoryProducerTest.java`
**Vorher:**
```java
EntityManagerFactory entityManagerFactory = producer.produce(databaseUser, databasePass);
assertThat(entityManagerFactory).isNotNull();
EntityManager entityManager = entityManagerFactory.createEntityManager();
assertThat(entityManager).isNotNull();
// ❌ Keine close() Aufrufe - Resource Leak!
```
**Nachher:**
```java
try (EntityManagerFactory entityManagerFactory = producer.produce(databaseUser, databasePass))
{
    assertThat(entityManagerFactory).isNotNull();
    try (EntityManager entityManager = entityManagerFactory.createEntityManager())
    {
        assertThat(entityManager).isNotNull();
        assertThat(entityManager.getTransaction()).isNotNull();
    }
    // ✅ EntityManager wird automatisch geschlossen
}
// ✅ EntityManagerFactory wird automatisch geschlossen
```
---
### 2. AbstractEntityManagerProducer - CDI Disposer Pattern
**Datei:** `lib/jpa/se.hibernate.postgres/src/main/java/.../AbstractEntityManagerProducer.java`
**Problem:** EntityManagerFactory wurde als lokale Variable erstellt und nie geschlossen.
**Änderungen:**
#### a) EntityManagerFactory als Feld speichern
**Vorher:**
```java
public abstract class AbstractEntityManagerProducer {
    private EntityManager entityManager;
    // ❌ EntityManagerFactory wird nicht gespeichert
}
```
**Nachher:**
```java
public abstract class AbstractEntityManagerProducer {
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;  // ✅ Als Feld
}
```
#### b) Disposer-Methode hinzugefügt
**Neu:**
```java
/**
 * Disposes the EntityManager and EntityManagerFactory.
 * Call this method from a method annotated with {@link jakarta.enterprise.inject.Disposes}
 * in subclasses.
 * 
 * @param entityManager the EntityManager to dispose
 */
protected void dispose(EntityManager entityManager)
{
    if (entityManager != null && entityManager.isOpen())
    {
        log.debug("closing entity manager: {}", entityManager);
        entityManager.close();
    }
    if (entityManagerFactory != null && entityManagerFactory.isOpen())
    {
        log.debug("closing entity manager factory");
        entityManagerFactory.close();
    }
    this.entityManager = null;
    this.entityManagerFactory = null;
}
```
**Verwendung in Subklassen:**
```java
@ApplicationScoped
public class MyEntityManagerProducer extends AbstractEntityManagerProducer {
    @Produces
    @ApplicationScoped
    public EntityManager entityManager() {
        return produce();
    }
    // ✅ Disposer-Methode
    public void closeEntityManager(@Disposes EntityManager entityManager) {
        dispose(entityManager);
    }
    @Override
    public List<Class<?>> managedClasses() {
        return List.of(/* ... */);
    }
}
```
---
## 🎯 Warum ist das wichtig?
### **1. Resource Leaks vermeiden**
```java
// ❌ BAD - Resource Leak
EntityManager em = emf.createEntityManager();
// Wenn Exception auftritt, wird em nie geschlossen!
// ✅ GOOD - Automatisches Schließen
try (EntityManager em = emf.createEntityManager()) {
    // ...
} // em wird IMMER geschlossen, auch bei Exceptions
```
### **2. Datenbankverbindungen freigeben**
- EntityManager hält Datenbankverbindungen offen
- Ohne close() bleiben Connections im Pool belegt
- → Connection Pool Exhaustion
- → Neue Anfragen können keine Connections erhalten
### **3. Speicher freigeben**
- EntityManager cached Entities im Persistence Context
- Ohne close() bleibt der Cache im Speicher
- → Memory Leaks bei Long-Running Applications
### **4. Transaktionen korrekt abschließen**
- Offene Transactions können Locks halten
- → Deadlocks in anderen Transactions möglich
---
## 📊 Best Practices
### **Pattern 1: try-with-resources (Empfohlen für Tests)**
```java
@Test
void myTest() {
    try (EntityManagerFactory emf = createFactory();
         EntityManager em = emf.createEntityManager()) 
    {
        // Test code
    }
    // Automatisches Schließen in umgekehrter Reihenfolge:
    // 1. EntityManager
    // 2. EntityManagerFactory
}
```
### **Pattern 2: @BeforeEach / @AfterEach (OK für Test-Fixtures)**
```java
@BeforeEach
void setUp() {
    entityManagerFactory = createFactory();
    entityManager = entityManagerFactory.createEntityManager();
}
@AfterEach  
void tearDown() {
    if (entityManager != null && entityManager.isOpen()) {
        entityManager.close();
    }
    if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
        entityManagerFactory.close();
    }
}
```
### **Pattern 3: CDI Producer/Disposer (CDI Environments)**
```java
@Produces
@ApplicationScoped
public EntityManager produce() {
    // ...
}
public void dispose(@Disposes EntityManager em) {
    if (em != null && em.isOpen()) {
        em.close();
    }
}
```
---
## ✅ Ergebnis
- ✅ **Keine Resource Leaks mehr**
- ✅ **try-with-resources in Tests**
- ✅ **Disposer Pattern für CDI**
- ✅ **EntityManagerFactory wird korrekt geschlossen**
- ✅ **Build erfolgreich**: `BUILD SUCCESS`
---
## 📝 Weitere Hinweise
### **EntityManager Lifecycle in CDI:**
1. **Producer erzeugt** EntityManager
2. **CDI injiziert** in Beans
3. **Disposer schließt** bei Bean-Destruction
### **Wann schließt CDI die Resources?**
- `@RequestScoped`: Am Ende des HTTP-Requests
- `@ApplicationScoped`: Beim Shutdown der Application
- `@Dependent`: Wenn Owner Bean destroyed wird
### **Transaktions-Management:**
```java
// ✅ Immer Transactions korrekt abschließen
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();
    // ... operations
    tx.commit();
} catch (Exception e) {
    if (tx.isActive()) {
        tx.rollback();
    }
    throw e;
}
```
---
## 🏆 Fazit
Alle AutoCloseable-Ressourcen werden jetzt korrekt verwaltet:
- ✅ Tests verwenden try-with-resources
- ✅ CDI Producer haben Disposer
- ✅ Keine Resource Leaks mehr
- ✅ Bessere Stabilität
**Best Practice:** Immer `try-with-resources` verwenden für AutoCloseable!
---
## 🔄 Update: CDI-Managed Repository Warnings (2026-02-26)
### Problem: False-Positive Warnings für CDI-Managed Repositories
**Situation:**
- `AbstractRepository` implementiert `AutoCloseable`
- Service- und Repository-Klassen verwenden `repository()` Aufrufe
- Compiler warnt vor "resource leak" weil kein try-with-resources verwendet wird
- **ABER**: Die Repositories sind CDI-managed und dürfen NICHT manuell geschlossen werden!
### Lösung: @SuppressWarnings mit Dokumentation
**Betroffene Klassen:**
1. `TaskGroupServiceJPA`
2. `TaskServiceJPA`
3. `TaskGroupRepositoryJPA`
4. `TaskRepositoryJPA`
**Fix:**
```java
/**
 * Note: The repository is a CDI-managed bean and should NOT be closed manually.
 * The @SuppressWarnings("resource") annotation suppresses false-positive warnings
 * about try-with-resources for the repository() calls.
 */
@SuppressWarnings("resource") // Repository is CDI-managed, not manually closed
public abstract class TaskGroupServiceJPA implements TaskGroupEntityService<...>
{
    protected abstract TaskGroupRepositoryJPA repository();
    // Uses repository() without try-with-resources - correct for CDI!
    public TaskGroupJPA create(TaskGroupJPA entity) {
        return repository().create(entity);
    }
}
```
---
## 🎯 Warum ist das korrekt?
### **CDI-Lifecycle Management**
```java
// ❌ FALSCH - Würde CDI-Lifecycle zerstören!
try (TaskGroupRepositoryJPA repo = repository()) {
    repo.create(entity);
}
// CDI Container verliert Kontrolle über Bean!
// ✅ RICHTIG - CDI verwaltet Lifecycle
TaskGroupRepositoryJPA repo = repository();
repo.create(entity);
// CDI schließt Bean bei Container-Shutdown
```
### **Wann schließt CDI die Repository-Beans?**
| Scope | Lifecycle-Ende |
|-------|----------------|
| `@ApplicationScoped` | Application Shutdown |
| `@RequestScoped` | Ende des HTTP-Requests |
| `@Dependent` | Wenn Owner-Bean destroyed wird |
### **EntityManager-Verwaltung**
```java
@ApplicationScoped
public class MyRepository extends AbstractRepository<MyEntity, Long> {
    @PersistenceContext
    private EntityManager entityManager; // ← CDI injiziert
    @Override
    protected EntityManager entityManager() {
        return entityManager; // ← NICHT manuell schließen!
    }
}
```
**CDI managed:**
- ✅ EntityManager wird von CDI injiziert
- ✅ CDI schließt EntityManager automatisch
- ✅ Kein manuelles close() nötig
---
## 📊 Pattern-Übersicht
### **Pattern 1: CDI-Managed Repository (Production)**
```java
@ApplicationScoped
public class MyService {
    @Inject
    private MyRepository repository; // CDI-managed
    @SuppressWarnings("resource") // Korrekt!
    public void doSomething() {
        repository.create(entity);
        // Kein close() - CDI verwaltet Lifecycle
    }
}
```
### **Pattern 2: Manuelles Repository (Tests ohne CDI)**
```java
@Test
void testWithManualRepository() {
    try (MyRepository repo = new MyRepositoryImpl(entityManager)) {
        repo.create(entity);
    } // Manuelles close() nur bei manueller Erstellung!
}
```
### **Pattern 3: EntityManager in Tests**
```java
@BeforeEach
void setup() {
    entityManager = emf.createEntityManager();
    repository = new MyRepositoryImpl(entityManager);
}
@AfterEach
void teardown() {
    if (entityManager != null && entityManager.isOpen()) {
        entityManager.close(); // Manuelles close() für Test-Setup
    }
}
```
---
## ✅ Zusammenfassung
### **Was wurde gemacht:**
1. ✅ `@SuppressWarnings("resource")` zu allen Service-Klassen hinzugefügt
2. ✅ `@SuppressWarnings("resource")` zu allen Repository-Klassen hinzugefügt
3. ✅ JavaDoc-Kommentare mit Erklärung hinzugefügt
4. ✅ Dokumentiert, warum das korrekt ist
### **Warum @SuppressWarnings richtig ist:**
- ✅ Repositories sind **CDI-managed**
- ✅ **CDI verwaltet den Lifecycle**
- ✅ Manuelles `close()` würde **CDI-Lifecycle zerstören**
- ✅ Warning ist ein **False Positive**
### **Wann NICHT @SuppressWarnings verwenden:**
- ❌ Wenn Repository manuell erstellt wird (z.B. in Tests)
- ❌ Wenn EntityManager manuell erstellt wird
- ❌ Wenn keine CDI-Verwaltung vorhanden ist
---
## 🏆 Finale Build-Ergebnis
```
[INFO] BUILD SUCCESS
[INFO] Total time: 03:12 min
```
- ✅ Alle Module kompilieren
- ✅ Keine false-positive Warnings mehr
- ✅ CDI-Lifecycle korrekt implementiert
- ✅ Dokumentation vorhanden
**Best Practice für CDI-managed Resources: @SuppressWarnings("resource") mit Dokumentation!**
