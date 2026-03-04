package io.quarkiverse.renarde.jpa.deployment;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;

import io.quarkiverse.renarde.jpa.NamedBlob;
import io.quarkiverse.renarde.util.JavaExtensions;
import io.quarkus.panache.common.deployment.EntityField;
import io.quarkus.panache.common.deployment.EntityModel;
import io.quarkus.panache.common.deployment.MetamodelInfo;

/**
 * Represents a field in a JPA entity model with type, validation, and relation information.
 * Refactored to use composition for better separation of concerns.
 */
public class ModelField {

    public static enum Type {
        LargeText,
        Text,
        Number,
        Checkbox,
        DateTimeLocal,
        Date,
        Time,
        Timestamp,
        Enum,
        Relation,
        MultiRelation,
        Ignore,
        MultiMultiRelation,
        Binary,
        JSON;
    }

    // Constant DotNames for annotations
    private static final DotName DOTNAME_MANYTOMANY = DotName.createSimple(ManyToMany.class.getName());
    private static final DotName DOTNAME_MANYTOONE = DotName.createSimple(ManyToOne.class.getName());
    private static final DotName DOTNAME_ONETOMANY = DotName.createSimple(OneToMany.class.getName());
    private static final DotName DOTNAME_ONETOONE = DotName.createSimple(OneToOne.class.getName());
    private static final DotName DOTNAME_ENUMERATED = DotName.createSimple(Enumerated.class.getName());
    private static final DotName DOTNAME_COLUMN = DotName.createSimple(Column.class.getName());
    private static final DotName DOTNAME_JOIN_COLUMN = DotName.createSimple(JoinColumn.class.getName());
    private static final DotName DOTNAME_LENGTH = DotName.createSimple(Length.class.getName());
    private static final DotName DOTNAME_SIZE = DotName.createSimple(Size.class.getName());
    private static final DotName DOTNAME_JDBC_TYPE_CODE = DotName.createSimple(JdbcTypeCode.class.getName());
    private static final DotName DOTNAME_TYPES = DotName.createSimple(Types.class.getName());
    private static final DotName DOTNAME_LOB = DotName.createSimple(Lob.class.getName());
    private static final DotName DOTNAME_ID = DotName.createSimple(Id.class.getName());
    private static final DotName DOTNAME_TRANSIENT = DotName.createSimple(Transient.class.getName());
    private static final DotName DOTNAME_ENTITY = DotName.createSimple(Entity.class.getName());
    private static final DotName DOTNAME_MAPPED_SUPERCLASS = DotName.createSimple(MappedSuperclass.class.getName());
    private static final DotName DOTNAME_GENERATED_VALUE = DotName.createSimple(GeneratedValue.class.getName());
    private static final DotName DOTNAME_NOT_NULL = DotName.createSimple(NotNull.class.getName());
    private static final DotName DOTNAME_NOT_EMPTY = DotName.createSimple(NotEmpty.class.getName());
    private static final DotName DOTNAME_NOT_BLANK = DotName.createSimple(NotBlank.class.getName());
    private static final DotName DOTNAME_URL = DotName.createSimple(URL.class.getName());
    public static final String NAMED_BLOB_DESCRIPTOR = "L" + NamedBlob.class.getName().replace('.', '/') + ";";

    // ========================================================================
    // COMPOSED OBJECTS - New architecture
    // ========================================================================

    private final FieldTypeInfo typeInfo;
    private final FieldRelationInfo relationInfo;
    private final FieldValidationInfo validationInfo;

    // ========================================================================
    // PUBLIC FIELDS - For backward compatibility
    // ========================================================================

    // For views - delegated to composed objects
    public String name;
    public String label;
    public Type type;
    public String relationClass;
    public String relationIdFieldName;
    public long min, max;
    public double step;
    public String help;
    public boolean required;

    // For processor
    public EntityField entityField;
    public EntityField inverseField;
    public List<AnnotationInstance> validation = new ArrayList<>();
    public String signature;
    public boolean relationOwner;
    public boolean id;
    public boolean generatedValue;
    public String relationIdFieldClass;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ModelField(EntityField entityField, String entityClass, MetamodelInfo metamodelInfo, IndexView index) {
        this.entityField = entityField;
        this.name = entityField.name;
        this.label = JavaExtensions.capitalised(this.name);

        ClassInfo classInfo = index.getClassByName(DotName.createSimple(entityClass));
        FieldInfo field = classInfo.field(entityField.name);
        this.signature = field.genericSignature();

        // Initialize composed objects
        this.typeInfo = new FieldTypeInfo(entityField);
        this.relationInfo = new FieldRelationInfo();
        this.validationInfo = new FieldValidationInfo();

        // Determine field properties
        this.id = field.annotation(DOTNAME_ID) != null;
        this.generatedValue = field.annotation(DOTNAME_GENERATED_VALUE) != null;

        // Process field
        processFieldType(field, entityField, metamodelInfo, index);
        processValidation(field, metamodelInfo);
        syncPublicFields();
    }

    // ========================================================================
    // FIELD PROCESSING
    // ========================================================================

    private void processFieldType(FieldInfo field, EntityField entityField, MetamodelInfo metamodelInfo, IndexView index) {
        AnnotationInstance oneToOne = field.annotation(DOTNAME_ONETOONE);
        AnnotationInstance column = field.annotation(DOTNAME_COLUMN);
        AnnotationInstance jdbcTypeCode = field.annotation(DOTNAME_JDBC_TYPE_CODE);

        // JSON type
        if (jdbcTypeCode != null && jdbcTypeCode.value().asInt() == SqlTypes.JSON) {
            typeInfo.type = Type.JSON;
            return;
        }

        // Numeric types
        if (processNumericType(entityField)) {
            return;
        }

        // Character type
        if (entityField.descriptor.equals("C")) {
            typeInfo.type = Type.Text;
            typeInfo.min = 1;
            typeInfo.max = 1;
            return;
        }

        // Boolean type
        if (entityField.descriptor.equals("Z") || entityField.descriptor.equals("Ljava/lang/Boolean;")) {
            typeInfo.type = Type.Checkbox;
            return;
        }

        // Binary types
        if (entityField.descriptor.equals("[B")
                || entityField.descriptor.equals("Ljava/sql/Blob;")
                || entityField.descriptor.equals(NAMED_BLOB_DESCRIPTOR)) {
            typeInfo.type = Type.Binary;
            return;
        }

        // String types
        if (entityField.descriptor.equals("Ljava/lang/String;")) {
            processStringType(field, column, jdbcTypeCode);
            return;
        }

        // Date/Time types
        if (processDateTimeType(entityField)) {
            return;
        }

        // Enum
        if (field.hasAnnotation(DOTNAME_ENUMERATED)) {
            typeInfo.type = Type.Enum;
            return;
        }

        // Relations
        if (processRelations(field, entityField, metamodelInfo, index, oneToOne)) {
            return;
        }

        // Check if it's an enum by class info
        checkEnumByClassInfo(field, index);
    }

    private boolean processNumericType(EntityField entityField) {
        if (entityField.descriptor.equals("B")) {
            typeInfo.type = Type.Number;
            typeInfo.min = Byte.MIN_VALUE;
            typeInfo.max = Byte.MAX_VALUE;
            typeInfo.step = 1;
            return true;
        } else if (entityField.descriptor.equals("S")) {
            typeInfo.type = Type.Number;
            typeInfo.min = Short.MIN_VALUE;
            typeInfo.max = Short.MAX_VALUE;
            typeInfo.step = 1;
            return true;
        } else if (entityField.descriptor.equals("I") || entityField.descriptor.equals("Ljava/lang/Integer;")) {
            typeInfo.type = Type.Number;
            typeInfo.min = Integer.MIN_VALUE;
            typeInfo.max = Integer.MAX_VALUE;
            typeInfo.step = 1;
            return true;
        } else if (entityField.descriptor.equals("J") || entityField.descriptor.equals("Ljava/lang/Long;")) {
            typeInfo.type = Type.Number;
            typeInfo.min = Long.MIN_VALUE;
            typeInfo.max = Long.MAX_VALUE;
            typeInfo.step = 1;
            return true;
        } else if (entityField.descriptor.equals("D") || entityField.descriptor.equals("Ljava/lang/Double;")
                || entityField.descriptor.equals("F") || entityField.descriptor.equals("Ljava/lang/Float;")) {
            typeInfo.type = Type.Number;
            typeInfo.step = 0.00001;
            return true;
        }
        return false;
    }

    private void processStringType(FieldInfo field, AnnotationInstance column, AnnotationInstance jdbcTypeCode) {
        AnnotationInstance length = field.annotation(DOTNAME_LENGTH);
        AnnotationInstance size = field.annotation(DOTNAME_SIZE);

        boolean isLargeText = (column != null && column.value("length") != null && column.value("length").asInt() > 255)
                || (length != null && length.value("max") != null && length.value("max").asInt() > 255)
                || (size != null && size.value("max") != null && size.value("max").asInt() > 255)
                || (jdbcTypeCode != null && jdbcTypeCode.value() != null && jdbcTypeCode.value().asInt() == Types.LONGVARCHAR)
                || field.hasAnnotation(DOTNAME_LOB);

        typeInfo.type = isLargeText ? Type.LargeText : Type.Text;
    }

    private boolean processDateTimeType(EntityField entityField) {
        if (entityField.descriptor.equals("Ljava/util/Date;") || entityField.descriptor.equals("Ljava/time/LocalDateTime;")) {
            typeInfo.type = Type.DateTimeLocal;
            return true;
        } else if (entityField.descriptor.equals("Ljava/time/LocalDate;")) {
            typeInfo.type = Type.Date;
            return true;
        } else if (entityField.descriptor.equals("Ljava/time/LocalTime;")) {
            typeInfo.type = Type.Time;
            return true;
        } else if (entityField.descriptor.equals("Ljava/sql/Timestamp;")) {
            typeInfo.type = Type.Timestamp;
            return true;
        }
        return false;
    }

    private boolean processRelations(FieldInfo field, EntityField entityField, MetamodelInfo metamodelInfo,
            IndexView index, AnnotationInstance oneToOne) {
        // OneToMany
        if (field.hasAnnotation(DOTNAME_ONETOMANY)) {
            return processOneToMany(field, metamodelInfo, index);
        }

        // ManyToMany
        if (field.hasAnnotation(DOTNAME_MANYTOMANY)) {
            return processManyToMany(field, metamodelInfo, index);
        }

        // OneToOne with mappedBy
        if (oneToOne != null && oneToOne.value("mappedBy") != null) {
            typeInfo.type = Type.Ignore;
            relationInfo.relationOwner = false;
            return true;
        }

        // ManyToOne or OneToOne (owning side)
        if (field.hasAnnotation(DOTNAME_MANYTOONE) || (oneToOne != null && oneToOne.value("mappedBy") == null)) {
            return processManyToOneOrOneToOne(entityField, index);
        }

        return false;
    }

    private boolean processOneToMany(FieldInfo field, MetamodelInfo metamodelInfo, IndexView index) {
        typeInfo.type = Type.MultiRelation;
        relationInfo.relationClass = field.type().asParameterizedType().arguments().get(0).name().toString();
        EntityModel relationModel = metamodelInfo.getEntityModel(relationInfo.relationClass);
        AnnotationValue mappedBy = field.annotation(DOTNAME_ONETOMANY).value("mappedBy");
        String inverseFieldName = mappedBy.asString();
        relationInfo.inverseField = relationModel.fields.get(inverseFieldName);
        relationInfo.relationOwner = false;

        FieldInfo relationIdField = findRelationIdField(relationInfo.relationClass, index);
        if (relationIdField != null) {
            relationInfo.relationIdFieldName = relationIdField.name();
            relationInfo.relationIdFieldClass = relationIdField.type().asClassType().name().toString();
        }
        return true;
    }

    private boolean processManyToMany(FieldInfo field, MetamodelInfo metamodelInfo, IndexView index) {
        typeInfo.type = Type.MultiMultiRelation;
        relationInfo.relationClass = field.type().asParameterizedType().arguments().get(0).name().toString();
        EntityModel relationModel = metamodelInfo.getEntityModel(relationInfo.relationClass);
        AnnotationValue mappedBy = field.annotation(DOTNAME_MANYTOMANY).value("mappedBy");

        if (mappedBy != null) {
            // Non-owning side
            String inverseFieldName = mappedBy.asString();
            relationInfo.inverseField = relationModel.fields.get(inverseFieldName);
            relationInfo.relationOwner = false;
        } else {
            // Owning side - find inverse
            ClassInfo relationClassInfo = index.getClassByName(DotName.createSimple(relationInfo.relationClass));
            for (FieldInfo relationField : relationClassInfo.fields()) {
                AnnotationInstance manyToMany = relationField.annotation(DOTNAME_MANYTOMANY);
                if (manyToMany != null) {
                    AnnotationValue value = manyToMany.value("mappedBy");
                    if (value != null && value.asString().equals(field.name())) {
                        relationInfo.inverseField = relationModel.fields.get(relationField.name());
                        break;
                    }
                }
            }
            relationInfo.relationOwner = true;
        }

        FieldInfo relationIdField = findRelationIdField(relationInfo.relationClass, index);
        if (relationIdField != null) {
            relationInfo.relationIdFieldName = relationIdField.name();
            relationInfo.relationIdFieldClass = relationIdField.type().asClassType().name().toString();
        }
        return true;
    }

    private boolean processManyToOneOrOneToOne(EntityField entityField, IndexView index) {
        typeInfo.type = Type.Relation;
        relationInfo.relationClass = entityField.descriptor.substring(1, entityField.descriptor.length() - 1).replace('/', '.');
        relationInfo.relationOwner = true;

        FieldInfo relationIdField = findRelationIdField(relationInfo.relationClass, index);
        if (relationIdField != null) {
            relationInfo.relationIdFieldName = relationIdField.name();
            relationInfo.relationIdFieldClass = relationIdField.type().asClassType().name().toString();
        }
        return true;
    }

    private void checkEnumByClassInfo(FieldInfo field, IndexView index) {
        ClassInfo fieldClassInfo = index.getClassByName(field.type().name());
        if (fieldClassInfo != null && fieldClassInfo.isEnum()) {
            typeInfo.type = Type.Enum;
        }
    }

    // ========================================================================
    // VALIDATION PROCESSING
    // ========================================================================

    private void processValidation(FieldInfo field, MetamodelInfo metamodelInfo) {
        validationInfo.help = "";
        boolean requiredAdded = false;

        AnnotationInstance column = field.annotation(DOTNAME_COLUMN);
        AnnotationInstance joinColumn = field.annotation(DOTNAME_JOIN_COLUMN);

        // Check for required based on nullable
        if ((column != null && column.value("nullable") != null && !column.value("nullable").asBoolean())
                || (joinColumn != null && joinColumn.value("nullable") != null && !joinColumn.value("nullable").asBoolean())
                        && !field.hasAnnotation(DOTNAME_NOT_BLANK)) {
            validationInfo.addValidation(AnnotationInstance.create(DOTNAME_NOT_BLANK, null, Collections.emptyList()));
            validationInfo.appendHelp("This field is required. ");
            validationInfo.required = true;
            requiredAdded = true;
        }

        // Process validation annotations
        for (DotName supportedValidationAnnotation : new DotName[] { DOTNAME_NOT_EMPTY, DOTNAME_NOT_NULL, DOTNAME_NOT_BLANK,
                DOTNAME_SIZE, DOTNAME_LENGTH, DOTNAME_URL }) {
            if (field.hasAnnotation(supportedValidationAnnotation)) {
                processValidationAnnotation(field, supportedValidationAnnotation, requiredAdded);
                requiredAdded = true;
            }
        }

        // Add column comment as help
        if (column != null && column.value("comment") != null && !column.value("comment").asString().isBlank()) {
            validationInfo.appendHelp(column.value("comment").asString());
        }
    }

    private void processValidationAnnotation(FieldInfo field, DotName annotationType, boolean requiredAdded) {
        AnnotationInstance annotation = field.annotation(annotationType);
        validationInfo.addValidation(annotation);

        if (annotationType == DOTNAME_NOT_EMPTY || annotationType == DOTNAME_NOT_NULL || annotationType == DOTNAME_NOT_BLANK) {
            validationInfo.required = true;
            if (!requiredAdded) {
                validationInfo.appendHelp("This field is required. ");
            }
        } else if (annotationType == DOTNAME_URL) {
            validationInfo.appendHelp("This field must be a URL. ");
        } else if (annotationType == DOTNAME_SIZE || annotationType == DOTNAME_LENGTH) {
            int min = withDefault(annotation.value("min"), 0);
            int max = withDefault(annotation.value("max"), Integer.MAX_VALUE);
            validationInfo.appendHelp("This field must be between " + min + " and " + max + " characters. ");
        }
    }

    // ========================================================================
    // SYNC PUBLIC FIELDS - For backward compatibility
    // ========================================================================

    private void syncPublicFields() {
        // Sync from composed objects to public fields
        this.type = typeInfo.type;
        this.min = typeInfo.min;
        this.max = typeInfo.max;
        this.step = typeInfo.step;

        this.relationClass = relationInfo.relationClass;
        this.relationIdFieldName = relationInfo.relationIdFieldName;
        this.relationIdFieldClass = relationInfo.relationIdFieldClass;
        this.relationOwner = relationInfo.relationOwner;
        this.inverseField = relationInfo.inverseField;

        this.help = validationInfo.help;
        this.required = validationInfo.required;
        this.validation = validationInfo.validation;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private <T> T withDefault(AnnotationValue value, T def) {
        if (value == null)
            return def;
        return (T) value.value();
    }

    private FieldInfo findRelationIdField(String relationClass, IndexView index) {
        ClassInfo classInfo = index.getClassByName(relationClass);
        if (classInfo == null) {
            return null;
        }
        if (classInfo.hasAnnotation(DOTNAME_ENTITY) || classInfo.hasAnnotation(DOTNAME_MAPPED_SUPERCLASS)) {
            for (FieldInfo fieldInfo : classInfo.fields()) {
                if (fieldInfo.hasAnnotation(DOTNAME_TRANSIENT)) {
                    continue;
                }
                if (fieldInfo.hasAnnotation(DOTNAME_ID)) {
                    return fieldInfo;
                }
            }
        }
        DotName superName = classInfo.superName();
        if (superName != null) {
            return findRelationIdField(superName.toString(), index);
        }
        return null;
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public String getClassName() {
        return entityField.descriptor.substring(1, entityField.descriptor.length() - 1).replace('/', '.');
    }

    public FieldTypeInfo getTypeInfo() {
        return typeInfo;
    }

    public FieldRelationInfo getRelationInfo() {
        return relationInfo;
    }

    public FieldValidationInfo getValidationInfo() {
        return validationInfo;
    }

    @Override
    public String toString() {
        return "ModelField " + name + " of type " + entityField.descriptor;
    }

    public static List<ModelField> loadModelFields(EntityModel entityModel, MetamodelInfo metamodelInfo,
            IndexView index) {
        List<ModelField> fields = new ArrayList<>();
        addFields(fields, entityModel, metamodelInfo, index);
        return fields;
    }

    private static void addFields(List<ModelField> fields, EntityModel entityModel, MetamodelInfo metamodelInfo,
            IndexView index) {
        for (Entry<String, EntityField> entry : entityModel.fields.entrySet()) {
            ModelField mf = new ModelField(entry.getValue(), entityModel.name, metamodelInfo, index);
            if (mf.type != Type.Ignore) {
                fields.add(mf);
            }
        }
        if (entityModel.superClassName != null) {
            EntityModel superModel = metamodelInfo.getEntityModel(entityModel.superClassName);
            if (superModel != null) {
                addFields(fields, superModel, metamodelInfo, index);
            }
        }
    }
}
