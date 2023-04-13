package com.brinvex.util.metamodelgen.processor.util;

public final class StringUtil {
    private static final String PROPERTY_PREFIX_GET = "get";
    private static final String PROPERTY_PREFIX_IS = "is";
    private static final String PROPERTY_PREFIX_HAS = "has";

    private StringUtil() {
    }

    public static boolean isProperty(String methodName, String returnTypeAsString) {
        if (methodName == null || "void".equals(returnTypeAsString)) {
            return false;
        }

        if (isValidPropertyName(methodName, PROPERTY_PREFIX_GET)) {
            return true;
        }

        if (isValidPropertyName(methodName, PROPERTY_PREFIX_IS) || isValidPropertyName(methodName, PROPERTY_PREFIX_HAS)) {
            return isBooleanGetter(returnTypeAsString);
        }

        return false;
    }

    private static boolean isBooleanGetter(String type) {
        return "Boolean".equals(type) || "java.lang.Boolean".equals(type);
    }

    private static boolean isValidPropertyName(String name, String prefix) {
        if (!name.startsWith(prefix)) {
            return false;
        }

        return name.length() >= prefix.length() + 1;
    }
}
