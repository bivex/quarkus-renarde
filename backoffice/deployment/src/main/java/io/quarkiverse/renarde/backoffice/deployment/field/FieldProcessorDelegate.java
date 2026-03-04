package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.ResultHandle;

/**
 * Delegate for field processing during bytecode generation.
 *
 * This class provides a bridge between RenardeBackofficeProcessor and the FieldProcessor strategy pattern,
 * allowing the processor to delegate field-specific logic to the appropriate processor implementation.
 */
public class FieldProcessorDelegate {

    private final FieldProcessorRegistry registry;

    public FieldProcessorDelegate() {
        this.registry = new FieldProcessorRegistry();
    }

    /**
     * Process a field during edit/create action bytecode generation.
     *
     * @param context the processing context containing all necessary information
     * @return the processed value handle, or null if value is set directly by the processor
     * @throws Exception if processing fails
     */
    public ResultHandle processField(FieldProcessingContext context) throws Exception {
        FieldProcessor processor = registry.findProcessor(context.getField());
        return processor.process(context);
    }

    /**
     * Process binary field with special handling for two parameters.
     *
     * @param context the processing context
     * @param parameterUnsetValue the unset parameter value
     * @param parameterValue the file upload value
     */
    public void processBinaryField(
            FieldProcessingContext context,
            ResultHandle parameterUnsetValue,
            ResultHandle parameterValue) {

        FieldProcessor processor = registry.findProcessor(context.getField());
        if (processor instanceof BinaryFieldProcessor) {
            ((BinaryFieldProcessor) processor).processBinaryField(context, parameterUnsetValue, parameterValue);
        } else {
            throw new RuntimeException("Field " + context.getField().name + " is not a Binary field");
        }
    }

    /**
     * Process multi-relation field with full collection handling.
     *
     * @param context the processing context
     */
    public void processMultiRelation(FieldProcessingContext context) {
        FieldProcessor processor = registry.findProcessor(context.getField());
        if (processor instanceof MultiRelationFieldProcessor) {
            ((MultiRelationFieldProcessor) processor).processMultiRelation(context);
        } else {
            throw new RuntimeException("Field " + context.getField().name + " is not a MultiRelation field");
        }
    }

    /**
     * Build a FieldProcessingContext for the given parameters.
     *
     * @param methodCreator the method creator
     * @param bytecodeCreator the bytecode creator
     * @param field the model field
     * @param entityClass the entity class name
     * @param entityVariable the entity variable
     * @param parameterValue the parameter value
     * @param mode the processing mode
     * @param parameterIndex the parameter index
     * @return the built context
     */
    public FieldProcessingContext buildContext(
            MethodCreator methodCreator,
            BytecodeCreator bytecodeCreator,
            ModelField field,
            String entityClass,
            AssignableResultHandle entityVariable,
            ResultHandle parameterValue,
            Mode mode,
            int parameterIndex) {

        return new FieldProcessingContext.Builder()
                .methodCreator(methodCreator)
                .bytecodeCreator(bytecodeCreator)
                .field(field)
                .entityClass(entityClass)
                .entityVariable(entityVariable)
                .parameterValue(parameterValue)
                .mode(mapMode(mode))
                .parameterIndex(parameterIndex)
                .build();
    }

    /**
     * Map RenardeBackofficeProcessor.Mode to FieldProcessingContext.Mode.
     */
    private io.quarkiverse.renarde.backoffice.deployment.field.Mode mapMode(Mode mode) {
        return mode == Mode.EDIT ? io.quarkiverse.renarde.backoffice.deployment.field.Mode.EDIT
                : io.quarkiverse.renarde.backoffice.deployment.field.Mode.CREATE;
    }

    /**
     * Mode enum matching RenardeBackofficeProcessor.Mode.
     */
    public enum Mode {
        EDIT,
        CREATE
    }

    /**
     * Get the processor registry (for testing purposes).
     *
     * @return the field processor registry
     */
    public FieldProcessorRegistry getRegistry() {
        return registry;
    }
}
