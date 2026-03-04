package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

/**
 * Processor for enum fields.
 */
public class EnumFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Enum;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        ResultHandle value = context.getBytecodeCreator().invokeStaticMethod(
                MethodDescriptor.ofMethod(BackUtil.class, "enumField", Enum.class, Class.class, String.class),
                context.getBytecodeCreator().loadClass(context.getField().getClassName()),
                context.getParameterValue());
        return context.getBytecodeCreator().checkCast(value, context.getField().entityField.descriptor);
    }

    @Override
    public int priority() {
        return 60;
    }
}
