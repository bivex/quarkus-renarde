package io.quarkiverse.renarde.jpa.deployment;

import io.quarkus.panache.common.deployment.EntityField;

/**
 * Relation information for a model field.
 * Contains data about entity relationships (ManyToOne, OneToMany, ManyToMany, etc.).
 */
public class FieldRelationInfo {

    public String relationClass;
    public String relationIdFieldName;
    public String relationIdFieldClass;
    public boolean relationOwner;
    public EntityField inverseField;

    public FieldRelationInfo() {
        this.relationOwner = true;
    }

    public FieldRelationInfo(String relationClass, String relationIdFieldName, String relationIdFieldClass) {
        this.relationClass = relationClass;
        this.relationIdFieldName = relationIdFieldName;
        this.relationIdFieldClass = relationIdFieldClass;
        this.relationOwner = true;
    }

    public boolean isRelation() {
        return relationClass != null;
    }

    public boolean isInverseRelation() {
        return inverseField != null;
    }
}
