package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

/**
 * Processor for JSON fields.
 */
public class JsonFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.JSON;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        ResultHandle value = context.getBytecodeCreator().invokeStaticMethod(
                MethodDescriptor.ofMethod(BackUtil.class, "jsonField", Object.class, String.class, String.class),
                context.getBytecodeCreator().load(context.getField().signature),
                context.getParameterValue());
        return context.getBytecodeCreator().checkCast(value, context.getField().entityField.descriptor);
    }

    @Override
    public int priority() {
        return 20;
    }
}
