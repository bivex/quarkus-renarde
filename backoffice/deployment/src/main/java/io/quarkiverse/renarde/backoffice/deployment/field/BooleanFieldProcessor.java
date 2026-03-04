package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;

/**
 * Processor for boolean fields (boolean and Boolean).
 */
public class BooleanFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Checkbox;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        String descriptor = context.getField().entityField.descriptor;

        if (descriptor.equals("Ljava/lang/Boolean;")) {
            // Boolean wrapper
            ResultHandle value = context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "booleanField", boolean.class, String.class),
                    context.getParameterValue());
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(Boolean.class, "valueOf", Boolean.class, boolean.class), value);
        } else {
            // boolean primitive
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "booleanField", boolean.class, String.class),
                    context.getParameterValue());
        }
    }

    @Override
    public int priority() {
        return 80;
    }
}
