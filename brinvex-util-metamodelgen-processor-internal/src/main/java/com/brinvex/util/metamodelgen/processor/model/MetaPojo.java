package com.brinvex.util.metamodelgen.processor.model;

import javax.lang.model.element.TypeElement;
import java.util.List;

public interface MetaPojo extends ImportContext {
	String getSimpleName();

	String getQualifiedName();

	String getPackageName();

	List<MetaAttribute> getMembers();

	String generateImports();

	String importType(String fqcn);

	TypeElement getTypeElement();

}
