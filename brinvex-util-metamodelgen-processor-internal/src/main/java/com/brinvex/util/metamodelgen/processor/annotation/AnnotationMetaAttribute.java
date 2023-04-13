package com.brinvex.util.metamodelgen.processor.annotation;


import com.brinvex.util.metamodelgen.processor.model.MetaAttribute;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.beans.Introspector;

public class AnnotationMetaAttribute implements MetaAttribute {

    private final Element element;
    private final AnnotationMetaPojo parent;

    public AnnotationMetaAttribute(AnnotationMetaPojo parent, Element element) {
        this.element = element;
        this.parent = parent;
    }

    @Override
    public String getAttributeNameDeclarationString() {
        String propertyName = getPropertyName();
        return "public static final String " +
                propertyName +
                " = " +
                "\"" +
                propertyName +
                "\"" +
                ";";
    }

    public String getPropertyName() {
        Elements elementsUtil = parent.getContext().getElementUtils();
        if (element.getKind() == ElementKind.FIELD) {
            return element.getSimpleName().toString();
        } else if (element.getKind() == ElementKind.METHOD) {
            String name = element.getSimpleName().toString();
            if (name.startsWith("get")) {
                return elementsUtil.getName(Introspector.decapitalize(name.substring("get".length()))).toString();
            } else if (name.startsWith("is")) {
                return (elementsUtil.getName(Introspector.decapitalize(name.substring("is".length())))).toString();
            }
            return elementsUtil.getName(Introspector.decapitalize(name)).toString();
        } else {
            return elementsUtil.getName(element.getSimpleName() + "/* " + element.getKind() + " */").toString();
        }
    }

    @Override
    public String toString() {
        return "AnnotationMetaAttribute" +
                "{element=" + element +
                '}';
    }
}
