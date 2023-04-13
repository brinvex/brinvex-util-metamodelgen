package com.brinvex.util.metamodelgen.processor.annotation;

import com.brinvex.util.metamodelgen.processor.Context;
import com.brinvex.util.metamodelgen.processor.ImportContextImpl;
import com.brinvex.util.metamodelgen.processor.model.ImportContext;
import com.brinvex.util.metamodelgen.processor.model.MetaAttribute;
import com.brinvex.util.metamodelgen.processor.model.MetaPojo;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnnotationMetaPojo implements MetaPojo {

    private final ImportContext importContext;
    private final TypeElement element;
    private final Map<String, MetaAttribute> members;
    private final Context context;

    public AnnotationMetaPojo(TypeElement element, Context context) {
        this.element = element;
        this.context = context;
        this.members = new LinkedHashMap<>();
        this.importContext = new ImportContextImpl();
        init();
    }

    public final Context getContext() {
        return context;
    }

    public final String getSimpleName() {
        return element.getSimpleName().toString();
    }

    public final String getQualifiedName() {
        return element.getQualifiedName().toString();
    }

    public final String getPackageName() {
        PackageElement packageOf = context.getElementUtils().getPackageOf(element);
        return context.getElementUtils().getName(packageOf.getQualifiedName()).toString();
    }

    public List<MetaAttribute> getMembers() {
        return new ArrayList<>(members.values());
    }

    private void mergeInMembers(Collection<MetaAttribute> attributes) {
        for (MetaAttribute attribute : attributes) {

            members.put(attribute.getPropertyName(), attribute);
        }
    }

    public void mergeInMembers(MetaPojo other) {
        mergeInMembers(other.getMembers());
    }

    public final String generateImports() {
        return importContext.generateImports();
    }

    public final String importType(String fqcn) {
        return importContext.importType(fqcn);
    }

    public final TypeElement getTypeElement() {
        return element;
    }

    @Override
    public String toString() {
        return "AnnotationMetaEntity" +
               "{element=" + element +
               ", members=" + members +
               '}';
    }

    protected final void init() {
        getContext().logMessage(Diagnostic.Kind.OTHER, "Initializing type " + getQualifiedName() + ".");

        List<? extends Element> methodsOfClass = ElementFilter.methodsIn(element.getEnclosedElements());
        List<Element> gettersAndSettersOfClass = new ArrayList<>();

        for (Element rawMethodOfClass : methodsOfClass) {
            if (isGetterOrSetter(rawMethodOfClass)) {
                gettersAndSettersOfClass.add(rawMethodOfClass);
            }
        }
        addMembers(gettersAndSettersOfClass);
    }

    /**
     * Check if method respects Java Bean conventions for getter and setters.
     *
     * @param methodOfClass method element
     * @return whether method respects Java Bean conventions.
     */
    private boolean isGetterOrSetter(Element methodOfClass) {
        ExecutableType methodType = (ExecutableType) methodOfClass.asType();
        String methodSimpleName = methodOfClass.getSimpleName().toString();
        List<? extends TypeMirror> methodParameterTypes = methodType.getParameterTypes();
        TypeMirror returnType = methodType.getReturnType();

        if (
                methodSimpleName.startsWith("set") &&
                methodParameterTypes.size() == 1 &&
                "void".equalsIgnoreCase(returnType.toString())) {
            return true;
        } else return (methodSimpleName.startsWith("get") || methodSimpleName.startsWith("is")) &&
                      methodParameterTypes.isEmpty() &&
                      !"void".equalsIgnoreCase(returnType.toString());
    }

    private void addMembers(List<? extends Element> membersOfClass) {
        for (Element memberOfClass : membersOfClass) {

            if (memberOfClass.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            MetaAttributeGenerationVisitor visitor = new MetaAttributeGenerationVisitor(this);
            AnnotationMetaAttribute result = memberOfClass.asType().accept(visitor, memberOfClass);
            if (result != null) {
                members.put(result.getPropertyName(), result);
            }
        }
    }
}
