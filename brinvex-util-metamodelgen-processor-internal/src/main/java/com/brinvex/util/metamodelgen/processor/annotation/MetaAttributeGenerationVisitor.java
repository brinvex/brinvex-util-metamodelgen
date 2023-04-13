package com.brinvex.util.metamodelgen.processor.annotation;

import com.brinvex.util.metamodelgen.processor.util.StringUtil;
import com.brinvex.util.metamodelgen.processor.util.TypeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor14;

public class MetaAttributeGenerationVisitor extends SimpleTypeVisitor14<AnnotationMetaAttribute, Element> {

    private final AnnotationMetaPojo entity;

    MetaAttributeGenerationVisitor(AnnotationMetaPojo entity) {
        this.entity = entity;
    }

    @Override
    public AnnotationMetaAttribute visitPrimitive(PrimitiveType t, Element element) {
        return new AnnotationMetaAttribute(entity, element);
    }

    @Override
    public AnnotationMetaAttribute visitArray(ArrayType t, Element element) {
        return new AnnotationMetaAttribute(entity, element);
    }

    @Override
    public AnnotationMetaAttribute visitTypeVariable(TypeVariable t, Element element) {
        return new AnnotationMetaAttribute(entity, element);
    }

    @Override
    public AnnotationMetaAttribute visitDeclared(DeclaredType declaredType, Element element) {
        return new AnnotationMetaAttribute(entity, element);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public AnnotationMetaAttribute visitExecutable(ExecutableType t, Element p) {
        if (!p.getKind().equals(ElementKind.METHOD)) {
            return null;
        }

        String string = p.getSimpleName().toString();
        if (!StringUtil.isProperty(string, TypeUtils.toTypeString(t.getReturnType()))) {
            return null;
        }

        TypeMirror returnType = t.getReturnType();
        return returnType.accept(this, p);
    }
}

