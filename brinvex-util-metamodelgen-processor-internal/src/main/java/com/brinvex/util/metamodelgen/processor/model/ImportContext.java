package com.brinvex.util.metamodelgen.processor.model;

public interface ImportContext {

    String importType(String fqcn);

    String generateImports();
}
