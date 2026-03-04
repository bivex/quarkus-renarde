package io.quarkiverse.renarde.backoffice.deployment.field;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.quarkiverse.renarde.jpa.deployment.ModelField;

/**
 * Registry for field processors.
 *
 * Processors are checked in priority order (highest first).
 * The first processor that can handle a field is used.
 */
public class FieldProcessorRegistry {

    private final List<FieldProcessor> processors = new ArrayList<>();

    public FieldProcessorRegistry() {
        // Register default processors
        registerProcessor(new TextFieldProcessor());
        registerProcessor(new NumberFieldProcessor());
        registerProcessor(new BooleanFieldProcessor());
        registerProcessor(new DateFieldProcessor());
        registerProcessor(new EnumFieldProcessor());
        registerProcessor(new BinaryFieldProcessor());
        registerProcessor(new RelationFieldProcessor());
        registerProcessor(new MultiRelationFieldProcessor());
        registerProcessor(new JsonFieldProcessor());
    }

    /**
     * Register a field processor.
     *
     * @param processor the processor to register
     */
    public void registerProcessor(FieldProcessor processor) {
        processors.add(processor);
        // Sort by priority (highest first)
        processors.sort(Comparator.comparingInt(FieldProcessor::priority).reversed());
    }

    /**
     * Find a processor for the given field.
     *
     * @param field the model field
     * @return the processor, or null if none found
     */
    public FieldProcessor findProcessor(ModelField field) {
        for (FieldProcessor processor : processors) {
            if (processor.canHandle(field)) {
                return processor;
            }
        }
        throw new RuntimeException("No processor found for field: " + field.name + " of type " + field.type);
    }

    /**
     * Get all registered processors.
     *
     * @return the list of processors
     */
    public List<FieldProcessor> getProcessors() {
        return new ArrayList<>(processors);
    }
}
