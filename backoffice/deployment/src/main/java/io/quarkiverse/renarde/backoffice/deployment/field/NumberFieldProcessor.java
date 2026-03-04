package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;

/**
 * Processor for numeric fields (byte, short, int, long, float, double and wrapper classes).
 */
public class NumberFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Number;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        String descriptor = context.getField().entityField.descriptor;

        // Handle wrapper types
        switch (descriptor) {
            case "Ljava/lang/Integer;":
                return context.getBytecodeCreator().invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "integerWrapperField", Integer.class, String.class),
                        context.getParameterValue());
            case "Ljava/lang/Long;":
                return context.getBytecodeCreator().invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "longWrapperField", Long.class, String.class),
                        context.getParameterValue());
            case "Ljava/lang/Double;":
                return context.getBytecodeCreator().invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "doubleWrapperField", Double.class, String.class),
                        context.getParameterValue());
            case "Ljava/lang/Float;":
                return context.getBytecodeCreator().invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "floatWrapperField", Float.class, String.class),
                        context.getParameterValue());
            default:
                // Handle primitive types
                return processPrimitive(context, descriptor);
        }
    }

    private ResultHandle processPrimitive(FieldProcessingContext context, String descriptor) {
        Class<?> primitiveClass;
        switch (descriptor) {
            case "B":
                primitiveClass = byte.class;
                break;
            case "S":
                primitiveClass = short.class;
                break;
            case "I":
                primitiveClass = int.class;
                break;
            case "J":
                primitiveClass = long.class;
                break;
            case "F":
                primitiveClass = float.class;
                break;
            case "D":
                primitiveClass = double.class;
                break;
            default:
                throw new RuntimeException("Unknown number field descriptor: " + descriptor);
        }
        return context.getBytecodeCreator().invokeStaticMethod(
                MethodDescriptor.ofMethod(BackUtil.class, primitiveClass.getName() + "Field", primitiveClass, String.class),
                context.getParameterValue());
    }

    @Override
    public int priority() {
        return 90;
    }
}
