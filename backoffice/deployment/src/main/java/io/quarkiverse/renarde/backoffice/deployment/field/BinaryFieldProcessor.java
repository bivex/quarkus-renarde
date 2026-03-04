package io.quarkiverse.renarde.backoffice.deployment.field;

import java.sql.Blob;

import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkiverse.renarde.jpa.NamedBlob;
import org.jboss.resteasy.reactive.server.multipart.FormValue;

/**
 * Processor for binary fields (byte[], Blob, NamedBlob).
 */
public class BinaryFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Binary;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) throws Exception {
        // Binary fields are handled specially: they consume two parameters
        // and set the value directly, so we return null
        // The actual processing should be done before calling this processor
        return null;
    }

    /**
     * Special method for binary field processing since it needs two parameters.
     */
    public void processBinaryField(
            FieldProcessingContext context,
            ResultHandle parameterUnsetValue,
            ResultHandle parameterValue) {

        var bc = context.getBytecodeCreator();
        var field = context.getField();
        var descriptor = field.entityField.descriptor;

        BranchResult hasValueTest = bc.ifTrue(bc.invokeStaticMethod(
                MethodDescriptor.ofMethod(BackUtil.class, "isSet", boolean.class, FormValue.class),
                parameterValue));

        try (var hasValueTrueBranch = hasValueTest.trueBranch()) {
            ResultHandle uploadValue;
            if (descriptor.equals("[B")) {
                uploadValue = hasValueTrueBranch.invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "byteArrayField", byte[].class, FormValue.class),
                        parameterValue);
            } else if (descriptor.equals("Ljava/sql/Blob;")) {
                uploadValue = hasValueTrueBranch.invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "blobField", Blob.class, FormValue.class),
                        parameterValue);
            } else if (descriptor.equals(ModelField.NAMED_BLOB_DESCRIPTOR)) {
                uploadValue = hasValueTrueBranch.invokeStaticMethod(
                        MethodDescriptor.ofMethod(BackUtil.class, "namedBlobField", NamedBlob.class, FormValue.class),
                        parameterValue);
            } else {
                throw new RuntimeException("Unknown binary field descriptor: " + descriptor);
            }
            hasValueTrueBranch.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(context.getEntityClass(), field.entityField.getSetterName(), void.class,
                            field.entityField.descriptor),
                    context.getEntityVariable(), uploadValue);
        }

        try (var hasValueFalseBranch = hasValueTest.falseBranch()) {
            BranchResult unsetTest = hasValueFalseBranch.ifTrue(hasValueFalseBranch.invokeStaticMethod(
                    MethodDescriptor.ofMethod(BackUtil.class, "booleanField", boolean.class, String.class),
                    parameterUnsetValue));
            try (var unsetTrueBranch = unsetTest.trueBranch()) {
                // set to null
                unsetTrueBranch.invokeVirtualMethod(
                        MethodDescriptor.ofMethod(context.getEntityClass(), field.entityField.getSetterName(), void.class,
                                field.entityField.descriptor),
                        context.getEntityVariable(), unsetTrueBranch.loadNull());
            }
            unsetTest.falseBranch().close();
        }
    }

    @Override
    public int priority() {
        return 50;
    }
}
