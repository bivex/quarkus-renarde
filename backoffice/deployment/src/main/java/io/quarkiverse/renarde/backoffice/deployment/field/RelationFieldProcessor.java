package io.quarkiverse.renarde.backoffice.deployment.field;

import io.quarkiverse.renarde.backoffice.impl.BackUtil;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * Processor for single relation fields (ManyToOne, OneToOne).
 */
public class RelationFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.Relation;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        var bc = context.getBytecodeCreator();
        var field = context.getField();

        BranchResult branch = bc.ifTrue(bc.invokeStaticMethod(
                MethodDescriptor.ofMethod(BackUtil.class, "isSet", boolean.class, String.class),
                context.getParameterValue()));

        AssignableResultHandle valueVar = bc.createVariable(field.entityField.descriptor);

        try (var tb = branch.trueBranch()) {
            // Convert ID and find entity by ID
            ResultHandle value = convertId(tb, field.relationIdFieldClass, context.getParameterValue());
            value = tb.invokeStaticMethod(
                    MethodDescriptor.ofMethod(field.getClassName(), "findById", PanacheEntityBase.class, Object.class),
                    value);
            value = tb.checkCast(value, field.entityField.descriptor);
            tb.assign(valueVar, value);
        }

        try (var fb = branch.falseBranch()) {
            fb.assign(valueVar, fb.loadNull());
        }

        return valueVar;
    }

    private ResultHandle convertId(BytecodeCreator bc, String relationIdFieldClass, ResultHandle next) {
        if (relationIdFieldClass.equals("java.lang.String")) {
            return next;
        }
        // Convert using valueOf
        return bc.invokeStaticMethod(
                MethodDescriptor.ofMethod(relationIdFieldClass, "valueOf", relationIdFieldClass, String.class),
                next);
    }

    @Override
    public int priority() {
        return 40;
    }
}
