package io.quarkiverse.renarde.backoffice.deployment.field;

import java.util.ArrayList;
import java.util.Iterator;

import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkiverse.renarde.jpa.deployment.EntityField;
import io.quarkiverse.renarde.jpa.deployment.ModelField;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * Processor for multi-relation fields (OneToMany, ManyToMany).
 *
 * Note: This processor is more complex as it needs to handle the entire collection,
 * not just a single value. It sets the value directly and returns null.
 */
public class MultiRelationFieldProcessor implements FieldProcessor {

    @Override
    public boolean canHandle(ModelField field) {
        return field.type == ModelField.Type.MultiRelation || field.type == ModelField.Type.MultiMultiRelation;
    }

    @Override
    public ResultHandle process(FieldProcessingContext context) {
        // Multi-relation fields are handled specially
        // They don't return a value but set the collection directly
        return null;
    }

    /**
     * Process multi-relation field with full collection handling.
     */
    public void processMultiRelation(FieldProcessingContext context) {
        var m = context.getBytecodeCreator();
        var field = context.getField();
        var mode = context.getMode();
        var entityVariable = context.getEntityVariable();
        var entityClass = context.getEntityClass();

        AssignableResultHandle iterator = m.createVariable(Iterator.class);

        if (mode == FieldProcessingContext.Mode.EDIT) {
            // Clear previous relations
            ResultHandle relation = m.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(entityClass, field.entityField.getGetterName(), field.entityField.descriptor),
                    entityVariable);
            m.assign(iterator, m.invokeInterfaceMethod(
                    MethodDescriptor.ofMethod(Iterable.class, "iterator", Iterator.class), relation));

            try (var loop = m.whileLoop(bc -> bc.ifTrue(bc.invokeInterfaceMethod(
                    MethodDescriptor.ofMethod(Iterator.class, "hasNext", boolean.class), iterator))).block()) {

                ResultHandle next = loop.checkCast(
                        loop.invokeInterfaceMethod(MethodDescriptor.ofMethod(Iterator.class, "next", Object.class), iterator),
                        field.relationClass);

                EntityField inverseField = field.inverseField;
                if (inverseField != null) {
                    clearInverseRelation(loop, field, next, inverseField, entityVariable);
                }
            }

            // Clear the collection
            relation = m.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(entityClass, field.entityField.getGetterName(), field.entityField.descriptor),
                    entityVariable);
            m.invokeInterfaceMethod(MethodDescriptor.ofMethod(ArrayList.class, "clear", void.class), relation);
        } else {
            // Create empty list
            m.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(entityClass, field.entityField.getSetterName(), void.class, field.entityField.descriptor),
                    entityVariable,
                    m.newInstance(MethodDescriptor.ofConstructor(ArrayList.class)));
        }

        // Add new relations
        m.assign(iterator, m.invokeInterfaceMethod(
                MethodDescriptor.ofMethod(Iterable.class, "iterator", Iterator.class), context.getParameterValue()));

        try (var loop = m.whileLoop(bc -> bc.ifTrue(bc.invokeInterfaceMethod(
                MethodDescriptor.ofMethod(Iterator.class, "hasNext", boolean.class), iterator))).block()) {

            ResultHandle next = loop.checkCast(
                    loop.invokeInterfaceMethod(MethodDescriptor.ofMethod(Iterator.class, "next", Object.class), iterator),
                    String.class);

            ResultHandle id = convertId(loop, field.relationIdFieldClass, next);
            ResultHandle otherEntity = loop.invokeStaticMethod(
                    MethodDescriptor.ofMethod(field.relationClass, "findById", PanacheEntityBase.class, Object.class), id);
            otherEntity = loop.checkCast(otherEntity, field.relationClass);

            // Set inverse relation
            if (field.inverseField != null) {
                setInverseRelation(loop, field, otherEntity, entityVariable);
            }

            // Add to collection
            ResultHandle relation = loop.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(entityClass, field.entityField.getGetterName(), field.entityField.descriptor),
                    entityVariable);
            loop.invokeInterfaceMethod(MethodDescriptor.ofMethod(ArrayList.class, "add", boolean.class, Object.class),
                    relation, otherEntity);
        }
    }

    private void clearInverseRelation(BytecodeCreator loop, ModelField field, ResultHandle next,
            EntityField inverseField, ResultHandle entityVariable) {
        if (field.type == ModelField.Type.MultiMultiRelation) {
            ResultHandle inverseRelation = loop.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(field.relationClass, inverseField.getGetterName(), inverseField.descriptor),
                    next);
            loop.invokeInterfaceMethod(
                    MethodDescriptor.ofMethod(ArrayList.class, "remove", boolean.class, Object.class),
                    inverseRelation, entityVariable);
        } else {
            loop.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(field.relationClass, inverseField.getSetterName(), void.class, inverseField.descriptor),
                    next, loop.loadNull());
        }
    }

    private void setInverseRelation(BytecodeCreator loop, ModelField field, ResultHandle otherEntity,
            ResultHandle entityVariable) {
        ResultHandle inverseRelation = loop.invokeVirtualMethod(
                MethodDescriptor.ofMethod(field.relationClass, field.inverseField.getGetterName(), field.inverseField.descriptor),
                otherEntity);

        if (field.type == ModelField.Type.MultiMultiRelation) {
            loop.invokeInterfaceMethod(
                    MethodDescriptor.ofMethod(ArrayList.class, "add", boolean.class, Object.class),
                    inverseRelation, entityVariable);
        } else {
            loop.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(field.relationClass, field.inverseField.getSetterName(), void.class, field.inverseField.descriptor),
                    otherEntity, entityVariable);
        }
    }

    private ResultHandle convertId(BytecodeCreator bc, String relationIdFieldClass, ResultHandle next) {
        if (relationIdFieldClass.equals("java.lang.String")) {
            return next;
        }
        return bc.invokeStaticMethod(
                MethodDescriptor.ofMethod(relationIdFieldClass, "valueOf", relationIdFieldClass, String.class), next);
    }

    @Override
    public int priority() {
        return 30;
    }
}
