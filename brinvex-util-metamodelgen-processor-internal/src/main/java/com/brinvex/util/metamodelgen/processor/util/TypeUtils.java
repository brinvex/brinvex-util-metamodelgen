package com.brinvex.util.metamodelgen.processor.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TypeUtils {

    private static final Map<TypeKind, String> PRIMITIVE_WRAPPERS = new LinkedHashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(TypeKind.CHAR, "Character");

        PRIMITIVE_WRAPPERS.put(TypeKind.BYTE, "Byte");
        PRIMITIVE_WRAPPERS.put(TypeKind.SHORT, "Short");
        PRIMITIVE_WRAPPERS.put(TypeKind.INT, "Integer");
        PRIMITIVE_WRAPPERS.put(TypeKind.LONG, "Long");

        PRIMITIVE_WRAPPERS.put(TypeKind.BOOLEAN, "Boolean");

        PRIMITIVE_WRAPPERS.put(TypeKind.FLOAT, "Float");
        PRIMITIVE_WRAPPERS.put(TypeKind.DOUBLE, "Double");
    }

    private TypeUtils() {
    }

    public static String toTypeString(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return PRIMITIVE_WRAPPERS.get(type.getKind());
        }
        return TypeRenderingVisitor.toString(type);
    }

    public static boolean containsAnnotation(Element element, String... annotations) {
        assert element != null;
        assert annotations != null;

        List<String> annotationClassNames = new ArrayList<>();
        Collections.addAll(annotationClassNames, annotations);

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the provided annotation type is of the same type as the provided class, {@code false} otherwise.
     * This method uses the string class names for comparison. See also
     * <a href="http://www.retep.org/2009/02/getting-class-values-from-annotations.html">getting-class-values-from-annotations</a>.
     *
     * @param annotationMirror The annotation mirror
     * @param fqcn             the fully qualified class name to check against
     * @return {@code true} if the provided annotation type is of the same type as the provided class, {@code false} otherwise.
     */
    public static boolean isAnnotationMirrorOfType(AnnotationMirror annotationMirror, String fqcn) {
        assert annotationMirror != null;
        assert fqcn != null;
        String annotationClassName = annotationMirror.getAnnotationType().toString();

        return annotationClassName.equals(fqcn);
    }
}
