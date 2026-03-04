package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.ResultHandle;

/**
 * Context object for field processing during bytecode generation.
 *
 * Contains all necessary information for processing a single field:
 * - The field metadata
 * - The bytecode creator
 * - The entity variable
 * - The form parameter value
 * - Processing mode (CREATE/EDIT)
 */
public class FieldProcessingContext {

    private final MethodCreator methodCreator;
    private final BytecodeCreator bytecodeCreator;
    private final ModelField field;
    private final String entityClass;
    private final AssignableResultHandle entityVariable;
    private final ResultHandle parameterValue;
    private final Mode mode;
    private final int parameterIndex;

    public FieldProcessingContext(
            MethodCreator methodCreator,
            BytecodeCreator bytecodeCreator,
            ModelField field,
            String entityClass,
            AssignableResultHandle entityVariable,
            ResultHandle parameterValue,
            Mode mode,
            int parameterIndex) {
        this.methodCreator = methodCreator;
        this.bytecodeCreator = bytecodeCreator;
        this.field = field;
        this.entityClass = entityClass;
        this.entityVariable = entityVariable;
        this.parameterValue = parameterValue;
        this.mode = mode;
        this.parameterIndex = parameterIndex;
    }

    public MethodCreator getMethodCreator() {
        return methodCreator;
    }

    public BytecodeCreator getBytecodeCreator() {
        return bytecodeCreator;
    }

    public ModelField getField() {
        return field;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public AssignableResultHandle getEntityVariable() {
        return entityVariable;
    }

    public ResultHandle getParameterValue() {
        return parameterValue;
    }

    public Mode getMode() {
        return mode;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    /**
     * Builder for creating FieldProcessingContext instances.
     */
    public static class Builder {
        private MethodCreator methodCreator;
        private BytecodeCreator bytecodeCreator;
        private ModelField field;
        private String entityClass;
        private AssignableResultHandle entityVariable;
        private ResultHandle parameterValue;
        private Mode mode;
        private int parameterIndex;

        public Builder methodCreator(MethodCreator methodCreator) {
            this.methodCreator = methodCreator;
            return this;
        }

        public Builder bytecodeCreator(BytecodeCreator bytecodeCreator) {
            this.bytecodeCreator = bytecodeCreator;
            return this;
        }

        public Builder field(ModelField field) {
            this.field = field;
            return this;
        }

        public Builder entityClass(String entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder entityVariable(AssignableResultHandle entityVariable) {
            this.entityVariable = entityVariable;
            return this;
        }

        public Builder parameterValue(ResultHandle parameterValue) {
            this.parameterValue = parameterValue;
            return this;
        }

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder parameterIndex(int parameterIndex) {
            this.parameterIndex = parameterIndex;
            return this;
        }

        public FieldProcessingContext build() {
            return new FieldProcessingContext(
                    methodCreator,
                    bytecodeCreator,
                    field,
                    entityClass,
                    entityVariable,
                    parameterValue,
                    mode,
                    parameterIndex);
        }
    }
}
