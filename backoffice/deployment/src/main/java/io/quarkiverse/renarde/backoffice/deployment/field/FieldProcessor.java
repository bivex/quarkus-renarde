package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.ResultHandle;

/**
 * Strategy interface for processing different field types during bytecode generation.
 *
 * Each implementation handles a specific field type (Text, Number, Relation, etc.)
 * and generates the appropriate bytecode for reading form values and setting entity fields.
 */
public interface FieldProcessor {

    /**
     * Check if this processor can handle the given field.
     *
     * @param field the model field to check
     * @return true if this processor can handle the field
     */
    boolean canHandle(ModelField field);

    /**
     * Generate bytecode to process the field value from form parameters.
     *
     * @param context the processing context
     * @return the processed value handle, or null if value is set directly
     * @throws Exception if bytecode generation fails
     */
    ResultHandle process(FieldProcessingContext context) throws Exception;

    /**
     * Get the priority of this processor.
     * Higher priority processors are checked first.
     *
     * @return the priority (default 0)
     */
    default int priority() {
        return 0;
    }
}
