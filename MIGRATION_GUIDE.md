# Migration Guide: Quarkus Renarde Architectural Refactoring

This guide helps you migrate your code to use the improved architecture after the 2026-03-04 refactoring.

## Overview of Changes

### 1. ModelField Composition

**Before:** God Object with all logic in one class
```java
ModelField field = new ModelField(...);
String type = field.type;           // Direct field access
long min = field.min;                // Direct field access
boolean required = field.required;   // Direct field access
```

**After:** Composition with type-safe accessors
```java
ModelField field = new ModelField(...);

// Option 1: Use composed objects (recommended)
FieldTypeInfo typeInfo = field.getTypeInfo();
Type type = typeInfo.getType();
long min = typeInfo.getMin();
long max = typeInfo.getMax();

FieldValidationInfo validationInfo = field.getValidationInfo();
boolean required = validationInfo.isRequired();
List<AnnotationInstance> validations = validationInfo.getValidation();

// Option 2: Legacy public fields (still available for backward compatibility)
String type = field.type;           // Works but deprecated
long min = field.min;                // Works but deprecated
boolean required = field.required;   // Works but deprecated
```

### 2. Flash ↔ Validation Circular Dependency Removed

**Before:** Direct dependency between Flash and Validation
```java
// In your controller:
@Inject
Flash flash;

@Inject
Validation validation;

// Flash called validation methods
validation.loadErrorsFromFlash();
```

**After:** Using ValidationContext
```java
@Inject
Flash flash;

@Inject
Validation validation;

// ValidationContext handles errors automatically
// No need to call loadErrorsFromFlash() anymore

// If you need to access validation context:
@Inject
ValidationContext validationContext;
```

**Migration Steps:**
1. Remove any direct calls to `validation.loadErrorsFromFlash()`
2. Flash now automatically loads errors into ValidationContext
3. Validation automatically writes errors to ValidationContext

### 3. Field Processor Strategy Pattern

**Before:** 450-line if/else chain in RenardeBackofficeProcessor

**After:** Strategy pattern with 9 processors

This change is **internal** and doesn't affect user code. However, if you were extending the backoffice processor:

**Before:**
```java
// Custom field handling in editOrCreateAction
if (field.type == ModelField.Type.Text) {
    // Custom logic
}
```

**After:**
```java
// Create a custom processor
public class MyCustomFieldProcessor implements FieldProcessor {
    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Text;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        // Custom logic
    }
}

// Register it
FieldProcessorRegistry registry = new FieldProcessorRegistry();
registry.registerProcessor(new MyCustomFieldProcessor());
```

## Breaking Changes

### None!

All public APIs remain backward compatible. The refactoring focused on **internal architecture** while preserving all existing functionality.

## Recommended Migrations (Optional)

### 1. Use Composed Objects

Replace direct field access with composed objects for better type safety:

```java
// Old
if (field.type == ModelField.Type.Relation) {
    String relationClass = field.relationClass;
    boolean isOwner = field.relationOwner;
}

// New
if (field.getTypeInfo().getType() == ModelField.Type.Relation) {
    FieldRelationInfo relationInfo = field.getRelationInfo();
    String relationClass = relationInfo.getRelationClass();
    boolean isOwner = relationInfo.isRelationOwner();
}
```

### 2. Remove Direct Dependencies

If you were injecting both Flash and Validation:

```java
// Before
@Inject
Flash flash;
@Inject
Validation validation;

// After (if you only need flash scope)
@Inject
Flash flash;

// ValidationContext is automatically managed
```

### 3. Extend Field Processors

If you need custom field handling in backoffice:

```java
// Create your processor
public class CustomFieldProcessor implements FieldProcessor {
    @Override
    public boolean canHandle(ModelField field) {
        // Your logic
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        // Your logic
    }
}

// Register it in your processor
FieldProcessorDelegate delegate = new FieldProcessorDelegate();
delegate.getRegistry().registerProcessor(new CustomFieldProcessor());
```

## Testing Your Migration

1. **Compile your project** — no compilation errors should occur
2. **Run your tests** — all existing tests should pass
3. **Check deprecation warnings** — consider migrating away from deprecated APIs
4. **Verify backoffice generation** — CRUD controllers should work as before

## Rollback Plan

If you encounter issues:

1. The old public fields on ModelField still work
2. Flash/Validation behavior is unchanged from user perspective
3. Field processors are internal only

## Need Help?

- Check `ARCHITECTURE_ANALYSIS.md` for detailed architecture changes
- See `CRITICAL_ISSUES.md` for known issues
- Review `SERVICE_LAYER.md` for service layer examples
- Check `DTO_LAYER.md` for DTO patterns

## Summary

| Change | Breaking | Action Required |
|--------|-----------|-----------------|
| ModelField composition | No | Optional: Use getters |
| Flash/Validation cycle | No | None (automatic) |
| Field processors | No | None (internal) |
| Service/DTO layers | No | Optional: Use patterns |

**Bottom line:** Your code should work without changes. This refactoring was **architectural cleanup** without breaking changes.
