# Lazy Mapper Tests - TODO

## Status

Die Lazy-Mapper benötigen persistierte JPA-Entities mit nicht-null `id` und `version` Feldern.
Unit-Tests ohne Datenbank sind daher nicht möglich.

## Betroffene Mapper

1. `Map_Task_JPA_Lazy` - JPA → Lazy (Task)
2. `Map_TaskGroup_JPA_Lazy` - JPA → Lazy (TaskGroup)
3. `Map_Task_Lazy_JPA` - Lazy → JPA (Task)
4. `Map_TaskGroup_Lazy_JPA` - Lazy → JPA (TaskGroup)

## Grund

Die Konstruktoren der Lazy-DTOs (z.B. `TaskGroupDTOLazy(TaskGroupEntity in)`) verwenden:
```java
id      = requireNonNull(in.id());
version = requireNonNull(in.version());
```

Ohne Datenbankpersistierung sind diese Felder `null`, was zu `NullPointerException` führt.

## Empfehlung

Integration-Tests mit PostgreSQL erstellen, ähnlich wie:
- `Map_Task_JPA_DTO_IntegrationTest`
- `Map_TaskGroup_JPA_DTO_IntegrationTest`

Diese Tests verwenden `@DisabledOnServerNotListening` und `AbstractJPATest`.

## Aktuelle Test-Abdeckung

✅ **Getestet (44 Tests total):**
- Map_Task_JPA_DTO (7 unit + 6 integration tests)
- Map_TaskGroup_JPA_DTO (9 unit + 6 integration tests)
- Map_Task_DTO_JPA (8 unit tests)
- Map_TaskGroup_DTO_JPA (9 unit tests)

❌ **Nicht getestet (4 Mapper):**
- Map_Task_JPA_Lazy
- Map_TaskGroup_JPA_Lazy
- Map_Task_Lazy_JPA
- Map_TaskGroup_Lazy_JPA

Die Kern-Mapping-Logik ist durch die DTO-Mapper-Tests bereits abgedeckt.
Die Lazy-Mapper folgen dem gleichen Muster und verwenden die gleichen MapStruct-Mechanismen.
