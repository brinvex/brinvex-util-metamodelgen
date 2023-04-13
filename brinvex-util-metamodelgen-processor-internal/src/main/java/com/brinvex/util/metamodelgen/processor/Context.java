package com.brinvex.util.metamodelgen.processor;

import com.brinvex.util.metamodelgen.processor.model.MetaPojo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class Context {

    /**
     * Used for keeping track of parsed embeddable entities. These entities have to be kept separate since
     * they are lazily initialized.
     */
    private final Map<String, MetaPojo> metaPojos = new LinkedHashMap<>();

    private final ProcessingEnvironment pe;
    private final boolean logDebug;
    private final TypeElement generatedAnnotation;

    /**
     * Whether all mapping files are xml-mapping-metadata-complete. In this case no annotation processing will take
     * place.
     */
    private boolean addGeneratedAnnotation = true;
    private boolean addGenerationDate;
    private boolean addSuppressWarningsAnnotation;

    // keep track of all classes for which model have been generated
    private final Collection<String> generatedModelClasses = new LinkedHashSet<>();

    public Context(ProcessingEnvironment pe) {
        this.pe = pe;

        logDebug = Boolean.parseBoolean(pe.getOptions().get(MetamodelGenerator.DEBUG_OPTION));

        TypeElement java8AndBelowGeneratedAnnotation =
                pe.getElementUtils().getTypeElement("javax.annotation.Generated");
        if (java8AndBelowGeneratedAnnotation != null) {
            generatedAnnotation = java8AndBelowGeneratedAnnotation;
        } else {
            // Using the new name for this annotation in Java 9 and above
            generatedAnnotation = pe.getElementUtils().getTypeElement("javax.annotation.processing.Generated");
        }
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return pe;
    }

    public boolean addGeneratedAnnotation() {
        return addGeneratedAnnotation;
    }

    public TypeElement getGeneratedAnnotation() {
        return generatedAnnotation;
    }

    public void setAddGeneratedAnnotation(boolean addGeneratedAnnotation) {
        this.addGeneratedAnnotation = addGeneratedAnnotation;
    }

    public boolean addGeneratedDate() {
        return addGenerationDate;
    }

    public void setAddGenerationDate(boolean addGenerationDate) {
        this.addGenerationDate = addGenerationDate;
    }

    public boolean isAddSuppressWarningsAnnotation() {
        return addSuppressWarningsAnnotation;
    }

    public void setAddSuppressWarningsAnnotation(boolean addSuppressWarningsAnnotation) {
        this.addSuppressWarningsAnnotation = addSuppressWarningsAnnotation;
    }

    public Elements getElementUtils() {
        return pe.getElementUtils();
    }

    public Types getTypeUtils() {
        return pe.getTypeUtils();
    }

    public boolean containsMetaEmbeddable(String fqcn) {
        return metaPojos.containsKey(fqcn);
    }

    public MetaPojo getMetaEmbeddable(String fqcn) {
        return metaPojos.get(fqcn);
    }

    public void addMetaEmbeddable(String fqcn, MetaPojo metaPojo) {
        metaPojos.put(fqcn, metaPojo);
    }

    public Collection<MetaPojo> getMetaPojos() {
        return metaPojos.values();
    }

    void markGenerated(String name) {
        generatedModelClasses.add(name);
    }

    boolean isAlreadyGenerated(String name) {
        return generatedModelClasses.contains(name);
    }

    public void logMessage(Diagnostic.Kind type, String message) {
        if (!logDebug && type.equals(Diagnostic.Kind.OTHER)) {
            return;
        }
        pe.getMessager().printMessage(type, message);
    }

    @Override
    public String toString() {
        return "Context" +
               ", logDebug=" + logDebug +
               '}';
    }
}
