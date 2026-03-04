# Критические проблемы после рефакторинга

## 🔴 P0 — Критические (требуют немедленной attention)

### 1. Field Processors НЕ интегрированы

**Проблема:** Создано 9 field процессоров с Strategy pattern, но они **не используются** в `RenardeBackofficeProcessor`.

```bash
grep -c "FieldProcessor" RenardeBackofficeProcessor.java
# Результат: 0
```

**Состояние:** Процессоры созданы но изолированы. Старый код с 450-строчным if/else всё ещё работает.

**Решение:** Интегрировать `FieldProcessorRegistry` в `RenardeBackofficeProcessor`.

---

### 2. Router.java — 377 строк перегруженных методов

**Проблема:** `Router.java` содержит 66 методов для перегрузки URI генерации.

```java
// Пример: Method5V, Method7V, Method11V, Method13V...
public class Router {
    // 66 overloaded methods
}
```

**Сложность:** Высокая, но оправданная (type-safe URI routing).

**Статус:** ℹ️ Принимается как архитектурное решение.

---

### 3. ModelField — публичные поля для обратной совместимости

**Проблема:** 18 публичных полей остаются для обратной совместимости:

```java
public class ModelField {
    // Composed objects (new)
    private final FieldTypeInfo typeInfo;
    private final FieldRelationInfo relationInfo;
    private final FieldValidationInfo validationInfo;

    // Public fields (backward compatibility)
    public String name, label, type;
    public String relationClass, relationIdFieldName;
    public long min, max;
    public double step;
    public String help;
    public boolean required;
    public EntityField entityField, inverseField;
    public List<AnnotationInstance> validation;
    public String signature;
    public boolean relationOwner, id, generatedValue;
    public String relationIdFieldClass;
}
```

**Статус:** ⚠️ Временно допустимо для миграции.

**Долг:** Удалить публичные поля после миграции всех пользователей.

---

## 🟡 P1 — Важные (следующий refactor)

### 4. Service/DTO слои не в runtime

**Проблема:** Сервисный и DTO слои удалены из runtime (deployment-only deps).

```bash
# Эти классы были созданы но удалены:
runtime/src/main/java/io/quarkiverse/renarde/service/     # Удалён
runtime/src/main/java/io/quarkiverse/renarde/dto/        # Удалён
```

**Решение:** Перенести в отдельный опциональный модуль.

---

### 5. ModelField — 32 входящие ссылки

**Проблема:** После разбивки ModelField всё ещё имеет 32 входящие ссылки.

**Прогресс:** Было 228 → Стало 32 (-86%)

**Осталось:** Ещё можно снизить до <10

---

## 🟢 P2 — Улучшения (nice to have)

### 6. FieldInfo классы не используются напрямую

**Проблема:** `FieldTypeInfo`, `FieldRelationInfo`, `FieldValidationInfo` инкапсулированы в ModelField, но недоступны извне.

**Решение:** Добавить getters для composed objects:

```java
public FieldTypeInfo getTypeInfo() { return typeInfo; }
public FieldRelationInfo getRelationInfo() { return relationInfo; }
public FieldValidationInfo getValidationInfo() { return validationInfo; }
```

---

### 7. Отсутствует документация для миграции

**Проблема:** Нет гайда для миграции с старого ModelField API.

**Решение:** Создать `MIGRATION_GUIDE.md`.

---

## Сводная таблица

| # | Проблема | Срочность | Статус |
|---|----------|-----------|--------|
| 1 | Field processors не интегрированы | P0 | 🔴 |
| 2 | Router.java — 66 перегруженных методов | P2 | ℹ️ |
| 3 | ModelField — 18 публичных полей | P1 | ⚠️ |
| 4 | Service/DTO удалены из runtime | P1 | ⚠️ |
| 5 | ModelField — 32 входящие ссылки | P1 | ✅ |
| 6 | FieldInfo getters недоступны | P2 | 📝 |
| 7 | Нет гайда миграции | P2 | 📝 |

---

## Приоритеты действий

1. **Немедленно:** Интегрировать FieldProcessorRegistry в RenardeBackofficeProcessor
2. **В следующем спринте:** Добавить getters для FieldInfo классов
3. **Позже:** Создать MIGRATION_GUIDE.md
4. **Архитектурное решение:** Оценить необходимость refactoring Router.java

---

**Итого:** 1 критическая проблема (Field processors), 3 важных, 3 улучшения.
