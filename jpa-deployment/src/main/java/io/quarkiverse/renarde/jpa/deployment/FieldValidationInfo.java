package io.quarkiverse.renarde.jpa.deployment;

import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;

/**
 * Validation information for a model field.
 * Contains validation annotations, required flag, and help text.
 */
public class FieldValidationInfo {

    public boolean required;
    public String help;
    public List<AnnotationInstance> validation = new ArrayList<>();

    public FieldValidationInfo() {
        this.required = false;
        this.help = "";
    }

    public void addValidation(AnnotationInstance annotation) {
        validation.add(annotation);
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void appendHelp(String text) {
        this.help += text;
    }
}
