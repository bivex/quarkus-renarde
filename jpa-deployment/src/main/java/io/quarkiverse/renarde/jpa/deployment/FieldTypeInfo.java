package io.quarkiverse.renarde.jpa.deployment;

import io.quarkus.panache.common.deployment.EntityField;

/**
 * Type information for a model field.
 * Contains the field type, descriptor, and numeric constraints.
 */
public class FieldTypeInfo {

    public ModelField.Type type;
    public String descriptor;
    public long min;
    public long max;
    public double step;

    public FieldTypeInfo(EntityField entityField) {
        this.descriptor = entityField.descriptor;
        this.type = ModelField.Type.Text;
        this.min = 0;
        this.max = 0;
        this.step = 1.0;
    }

    public FieldTypeInfo(EntityField entityField, ModelField.Type type) {
        this.descriptor = entityField.descriptor;
        this.type = type;
        this.min = 0;
        this.max = 0;
        this.step = 1.0;
    }

    public FieldTypeInfo(EntityField entityField, ModelField.Type type, long min, long max, double step) {
        this.descriptor = entityField.descriptor;
        this.type = type;
        this.min = min;
        this.max = max;
        this.step = step;
    }
}
