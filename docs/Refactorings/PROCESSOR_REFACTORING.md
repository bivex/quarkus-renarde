# Build-Time Processor Refactoring

## Overview

The Quarkus Renarde build-time processors have been refactored to use the **Strategy Pattern** for field processing, reducing code complexity and improving maintainability.

## Problem

Before refactoring:
- `RenardeBackofficeProcessor.java` was 1150 lines
- `editOrCreateAction()` method was 450 lines
- Massive if/else chains for different field types
- Difficult to add new field types
- Hard to test and maintain

## Solution

After refactoring:
- **Strategy Pattern** for field processing
- Separate processor for each field type
- Cleaner main processor logic
- Easier to add new field types

## Architecture

```
RenardeBackofficeProcessor
         ↓
FieldProcessorRegistry
         ↓
┌────────────────────────────────────────┐
│  FieldProcessor (interface)            │
│  + canHandle(ModelField): boolean      │
│  + process(Context): ResultHandle      │
└────────────────────────────────────────┘
         ↓
┌──────────┬──────────┬──────────┬────────────┐
│  Text    │  Number  │  Date    │  Relation   │
│  Enum    │  Binary  │  JSON    │ MultiRelation│
└──────────┴──────────┴──────────┴────────────┘
```

## FieldProcessor Interface

```java
public interface FieldProcessor {
    boolean canHandle(ModelField field);
    ResultHandle process(FieldProcessingContext context);
    int priority(); // Higher = checked first
}
```

## Usage Example

```java
// In RenardeBackofficeProcessor
private FieldProcessorRegistry processorRegistry = new FieldProcessorRegistry();

// Instead of 450-line if/else chain:
for (ModelField field : fields) {
    FieldProcessor processor = processorRegistry.findProcessor(field);
    ResultHandle value = processor.process(context);
    if (value != null) {
        m.invokeVirtualMethod(setter, entityVariable, value);
    }
}
```

## Implemented Processors

| Processor | Type | Priority |
|-----------|------|----------|
| `TextFieldProcessor` | Text, LargeText | 100 |
| `NumberFieldProcessor` | byte, short, int, long, float, double | 90 |
| `BooleanFieldProcessor` | boolean, Boolean | 80 |
| `DateFieldProcessor` | Date, LocalDateTime, LocalDate, LocalTime, Timestamp | 70 |
| `EnumFieldProcessor` | Enum | 60 |
| `BinaryFieldProcessor` | byte[], Blob, NamedBlob | 50 |
| `RelationFieldProcessor` | ManyToOne, OneToOne | 40 |
| `MultiRelationFieldProcessor` | OneToMany, ManyToMany | 30 |
| `JsonFieldProcessor` | JSON | 20 |

## Adding a New Field Type

1. Create a new processor class:

```java
public class MyCustomFieldProcessor implements FieldProcessor {
    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.MyCustomType;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        // Generate bytecode for your field type
        return context.getBytecodeCreator().invokeStaticMethod(...);
    }

    @Override
    public int priority() {
        return 50; // Choose appropriate priority
    }
}
```

2. Register in `FieldProcessorRegistry`:

```java
public FieldProcessorRegistry() {
    registerProcessor(new MyCustomFieldProcessor());
    // ... other processors
}
```

## Benefits

| Before | After |
|--------|-------|
| 450-line method | 50-line method with delegation |
| Hard to add types | Add new processor class |
| Hard to test | Test processors independently |
| Duplicated code | Shared utilities in registry |

## File Structure

```
backoffice/deployment/src/main/java/io/quarkiverse/renarde/backoffice/deployment/
├── RenardeBackofficeProcessor.java (simplified)
└── field/
    ├── FieldProcessor.java (interface)
    ├── FieldProcessingContext.java (context data)
    ├── FieldProcessorRegistry.java (processor manager)
    ├── TextFieldProcessor.java
    ├── NumberFieldProcessor.java
    ├── BooleanFieldProcessor.java
    ├── DateFieldProcessor.java
    ├── EnumFieldProcessor.java
    ├── BinaryFieldProcessor.java
    ├── RelationFieldProcessor.java
    ├── MultiRelationFieldProcessor.java
    └── JsonFieldProcessor.java
```

## Migration Guide

The old `RenardeBackofficeProcessor` still works, but new code should use the processor registry.

To migrate existing code:

**Before:**
```java
if (field.type == ModelField.Type.Text) {
    // 50 lines of text processing
} else if (field.type == ModelField.Type.Number) {
    // 50 lines of number processing
} // ... 10 more types
```

**After:**
```java
FieldProcessor processor = processorRegistry.findProcessor(field);
ResultHandle value = processor.process(context);
```

## Performance

- **Compile-time**: No impact (processors are instantiated once)
- **Runtime**: No impact (generates same bytecode as before)
- **Memory**: Minimal (few extra objects during build)

## Testing

Each processor can be tested independently:

```java
@Test
public void testTextFieldProcessor() {
    ModelField field = new ModelField(...);
    field.type = ModelField.Type.Text;

    TextFieldProcessor processor = new TextFieldProcessor();
    assertTrue(processor.canHandle(field));
}
```
