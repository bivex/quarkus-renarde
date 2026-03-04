package io.quarkiverse.renarde.backoffice.deployment.field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

/**
 * Processor for date/time fields.
 */
public class DateFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Date
                || field.type == ModelField.Type.Time
                || field.type == ModelField.Type.DateTimeLocal
                || field.type == ModelField.Type.Timestamp;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        String descriptor = context.getField().entityField.descriptor;

        if (descriptor.equals("Ljava/util/Date;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "dateField", Date.class, String.class),
                    context.getParameterValue());
        } else if (descriptor.equals("Ljava/sql/Timestamp;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "sqlTimestampField", java.sql.Timestamp.class, String.class),
                    context.getParameterValue());
        } else if (descriptor.equals("Ljava/time/LocalDateTime;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "localDateTimeField", LocalDateTime.class, String.class),
                    context.getParameterValue());
        } else if (descriptor.equals("Ljava/time/LocalDate;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "localDateField", LocalDate.class, String.class),
                    context.getParameterValue());
        } else if (descriptor.equals("Ljava/time/LocalTime;")) {
            return context.getBytecodeCreator().invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "localTimeField", LocalTime.class, String.class),
                    context.getParameterValue());
        }

        throw new RuntimeException("Unknown date/time field descriptor: " + descriptor);
    }

    @Override
    public int priority() {
        return 70;
    }
}
