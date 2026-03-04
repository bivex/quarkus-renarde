package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;

/**
 * Processor for text fields (String, char, LargeText).
 */
public class TextFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Text || field.type == ModelField.Type.LargeText;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        String descriptor = context.getField().entityField.descriptor;

        if (descriptor.equals("Ljava/lang/String;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "stringField", String.class, String.class),
                    context.getParameterValue());
        } else if (descriptor.equals("C")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "charField", char.class, String.class),
                    context.getParameterValue());
        } else {
            throw new RuntimeException("Unknown text field descriptor: " + descriptor);
        }
    }

    @Override
    public int priority() {
        return 100; // High priority for common fields
    }
}
