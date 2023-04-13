package com.brinvex.util.metamodelgen.processor;

import com.brinvex.util.metamodelgen.processor.model.ImportContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ImportContextImpl implements ImportContext {

    private final Set<String> imports = new TreeSet<>();
    private final Map<String, String> simpleNames = new LinkedHashMap<>();

    public ImportContextImpl() {
    }

    public String importType(String fqcn) {
        String result = fqcn;

        boolean canBeSimple;

        String simpleName = unqualify(fqcn);
        if (simpleNames.containsKey(simpleName)) {
            String existingFqcn = simpleNames.get(simpleName);
            canBeSimple = existingFqcn.equals(fqcn);
        } else {
            canBeSimple = true;
            simpleNames.put(simpleName, fqcn);
            imports.add(fqcn);
        }

        if (imports.contains(fqcn) && canBeSimple) {
            result = unqualify(result);
        }

        return result;
    }

    public String generateImports() {
        StringBuilder builder = new StringBuilder();

        for (String next : imports) {
            builder.append("import ").append(next).append(";").append(System.lineSeparator());
        }

        return builder.toString();
    }

    private static String unqualify(String qualifiedName) {
        int loc = qualifiedName.lastIndexOf('.');
        return (loc < 0) ? qualifiedName : qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }
}
