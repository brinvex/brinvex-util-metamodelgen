package com.brinvex.util.metamodelgen.processor;

import com.brinvex.util.metamodelgen.processor.annotation.AnnotationMetaPojo;
import com.brinvex.util.metamodelgen.processor.model.MetaPojo;
import com.brinvex.util.metamodelgen.processor.util.StringUtil;
import com.brinvex.util.metamodelgen.processor.util.TypeUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Main annotation processor.
 */
@SupportedAnnotationTypes({
        MetamodelGenerator.GENERATE_PROP_NAMES_METAMODEL
})
@SupportedOptions({
        MetamodelGenerator.DEBUG_OPTION,
        MetamodelGenerator.ADD_GENERATION_DATE,
        MetamodelGenerator.ADD_GENERATED_ANNOTATION,
        MetamodelGenerator.ADD_SUPPRESS_WARNINGS_ANNOTATION
})
public class MetamodelGenerator extends AbstractProcessor {

    public static final String GENERATE_PROP_NAMES_METAMODEL = "com.brinvex.util.metamodelgen.annotations.GeneratePropNamesMetamodel";

    public static final String DEBUG_OPTION = "debug";
    public static final String ADD_GENERATION_DATE = "addGenerationDate";
    public static final String ADD_GENERATED_ANNOTATION = "addGeneratedAnnotation";
    public static final String ADD_SUPPRESS_WARNINGS_ANNOTATION = "addSuppressWarningsAnnotation";

    private static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = Boolean.FALSE;

    private Context context;

    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        context = new Context(env);

        String tmp = env.getOptions().get(MetamodelGenerator.ADD_GENERATED_ANNOTATION);
        if (tmp != null) {
            boolean addGeneratedAnnotation = Boolean.parseBoolean(tmp);
            context.setAddGeneratedAnnotation(addGeneratedAnnotation);
        }

        tmp = env.getOptions().get(MetamodelGenerator.ADD_GENERATION_DATE);
        boolean addGenerationDate = Boolean.parseBoolean(tmp);
        context.setAddGenerationDate(addGenerationDate);

        tmp = env.getOptions().get(MetamodelGenerator.ADD_SUPPRESS_WARNINGS_ANNOTATION);
        boolean addSuppressWarningsAnnotation = Boolean.parseBoolean(tmp);
        context.setAddSuppressWarningsAnnotation(addSuppressWarningsAnnotation);

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver() || annotations.size() == 0) {
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
        }

        Set<? extends Element> elements = roundEnvironment.getRootElements();
        for (Element element : elements) {
            if (isProcessablePojo(element)) {
                context.logMessage(Diagnostic.Kind.OTHER, "Processing annotated class " + element.toString());
                handleRootElementAnnotationMirrors(element);
            }
        }

        createMetaModelClasses();
        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }

    private void createMetaModelClasses() {

        // we cannot process the delayed entities in any order. There might be dependencies between them.
        // we need to process the top level entities first
        Collection<MetaPojo> toProcessEntities = context.getMetaPojos();
        while (!toProcessEntities.isEmpty()) {
            Set<MetaPojo> processedEntities = new LinkedHashSet<>();
            int toProcessCountBeforeLoop = toProcessEntities.size();
            for (MetaPojo entity : toProcessEntities) {
                if (context.isAlreadyGenerated(entity.getQualifiedName())) {
                    processedEntities.add(entity);
                    continue;
                }
                if (modelGenerationNeedsToBeDeferred(toProcessEntities, entity)) {
                    continue;
                }
                context.logMessage(
                        Diagnostic.Kind.OTHER, "Writing meta model for embeddable/mapped superclass" + entity
                );
                ClassWriter.writeFile(entity, context);
                context.markGenerated(entity.getQualifiedName());
                processedEntities.add(entity);
            }
            toProcessEntities.removeAll(processedEntities);
            if (toProcessEntities.size() >= toProcessCountBeforeLoop) {
                context.logMessage(
                        Diagnostic.Kind.ERROR, "Potential endless loop in generation of entities."
                );
            }
        }
    }

    private boolean modelGenerationNeedsToBeDeferred(Collection<MetaPojo> entities, MetaPojo containedEntity) {
        ContainsAttributeTypeVisitor visitor = new ContainsAttributeTypeVisitor(
                containedEntity.getTypeElement(), context
        );
        for (MetaPojo entity : entities) {
            if (entity.equals(containedEntity)) {
                continue;
            }
            for (Element subElement : ElementFilter.fieldsIn(entity.getTypeElement().getEnclosedElements())) {
                TypeMirror mirror = subElement.asType();
                if (!TypeKind.DECLARED.equals(mirror.getKind())) {
                    continue;
                }
                boolean contains = mirror.accept(visitor, subElement);
                if (contains) {
                    return true;
                }
            }
            for (Element subElement : ElementFilter.methodsIn(entity.getTypeElement().getEnclosedElements())) {
                TypeMirror mirror = subElement.asType();
                if (!TypeKind.DECLARED.equals(mirror.getKind())) {
                    continue;
                }
                boolean contains = mirror.accept(visitor, subElement);
                if (contains) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isProcessablePojo(Element element) {
        return TypeUtils.containsAnnotation(element, GENERATE_PROP_NAMES_METAMODEL);
    }

    private void handleRootElementAnnotationMirrors(final Element element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (!ElementKind.CLASS.equals(element.getKind())) {
                continue;
            }

            String fqn = ((TypeElement) element).getQualifiedName().toString();
            MetaPojo alreadyExistingMetaPojo = tryGettingExistingEntityFromContext(mirror, fqn);

            AnnotationMetaPojo metaEntity = new AnnotationMetaPojo((TypeElement) element, context);

            if (alreadyExistingMetaPojo != null) {
                metaEntity.mergeInMembers(alreadyExistingMetaPojo);
            }
            addMetaEntityToContext(mirror, metaEntity);
        }
    }

    private MetaPojo tryGettingExistingEntityFromContext(AnnotationMirror mirror, String fqn) {
        MetaPojo alreadyExistingMetaPojo = null;
        if (TypeUtils.isAnnotationMirrorOfType(mirror, GENERATE_PROP_NAMES_METAMODEL)) {
            alreadyExistingMetaPojo = context.getMetaEmbeddable(fqn);
        }
        return alreadyExistingMetaPojo;
    }

    private void addMetaEntityToContext(AnnotationMirror mirror, AnnotationMetaPojo metaEntity) {
        if (TypeUtils.isAnnotationMirrorOfType(mirror, GENERATE_PROP_NAMES_METAMODEL)) {
            context.addMetaEmbeddable(metaEntity.getQualifiedName(), metaEntity);
        }
    }


    static class ContainsAttributeTypeVisitor extends SimpleTypeVisitor14<Boolean, Element> {

        private final Context context;
        private final TypeElement type;

        ContainsAttributeTypeVisitor(TypeElement elem, Context context) {
            this.context = context;
            this.type = elem;
        }

        @Override
        public Boolean visitDeclared(DeclaredType declaredType, Element element) {
            TypeElement returnedElement = (TypeElement) context.getTypeUtils().asElement(declaredType);
            if (type.getQualifiedName().toString().equals(returnedElement.getQualifiedName().toString())) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        public Boolean visitExecutable(ExecutableType t, Element element) {
            if (!element.getKind().equals(ElementKind.METHOD)) {
                return Boolean.FALSE;
            }

            String string = element.getSimpleName().toString();
            if (!StringUtil.isProperty(string, TypeUtils.toTypeString(t.getReturnType()))) {
                return Boolean.FALSE;
            }

            TypeMirror returnType = t.getReturnType();
            return returnType.accept(this, element);
        }
    }
}
