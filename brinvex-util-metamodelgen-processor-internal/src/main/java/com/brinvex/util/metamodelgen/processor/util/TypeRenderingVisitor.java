package com.brinvex.util.metamodelgen.processor.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TypeRenderingVisitor extends SimpleTypeVisitor8<Object, Object> {

	private final StringBuilder sb = new StringBuilder();
	private final Set<TypeVariable> visitedTypeVariables = new LinkedHashSet<>();

	private TypeRenderingVisitor() {
	}

	public static String toString(TypeMirror typeMirror) {
		if ( typeMirror instanceof TypeVariable ) {
			// Top level type variables don't need to render the upper bound as `T extends Type`
			final Element typeVariableElement = ( (TypeVariable) typeMirror ).asElement();
			if (typeVariableElement instanceof TypeParameterElement) {
				if ( typeVariableElement.getEnclosingElement().getKind() == ElementKind.METHOD ) {
					// But for method level type variable we return the upper bound
					// because the type variable has no meaning except for that method
					typeMirror = ( (TypeVariable) typeMirror ).getUpperBound();
				}
				else {
					return typeVariableElement.toString();
				}
			}
			else {
				typeMirror = typeVariableElement.asType();
			}
		}
		else if ( typeMirror instanceof IntersectionType ) {
			// For top level type only the first type is relevant
			typeMirror = ( (IntersectionType) typeMirror ).getBounds().get( 0 );
		}
		final TypeRenderingVisitor typeRenderingVisitor = new TypeRenderingVisitor();
		typeMirror.accept( typeRenderingVisitor, null );
		return typeRenderingVisitor.sb.toString();
	}

	@Override
	public Object visitPrimitive(PrimitiveType t, Object o) {
		final String primitiveTypeName = getPrimitiveTypeName( t.getKind() );
		if ( primitiveTypeName != null ) {
			sb.append( primitiveTypeName );
		}
		return null;
	}

	private static String getPrimitiveTypeName(TypeKind kind) {
		return switch (kind) {
			case INT -> "int";
			case BOOLEAN -> "boolean";
			case BYTE -> "byte";
			case CHAR -> "char";
			case DOUBLE -> "double";
			case FLOAT -> "float";
			case LONG -> "long";
			case SHORT -> "short";
			case VOID -> "void";
			default -> null;
		};
	}

	@Override
	public Object visitNull(NullType t, Object o) {
		return null;
	}

	@Override
	public Object visitArray(ArrayType t, Object o) {
		t.getComponentType().accept( this, null );
		sb.append( "[]" );
		return t;
	}

	@Override
	public Object visitDeclared(DeclaredType t, Object o) {
		sb.append( t.asElement().toString() );
		List<? extends TypeMirror> typeArguments = t.getTypeArguments();
		if ( !typeArguments.isEmpty() ) {
			sb.append( '<' );
			typeArguments.get( 0 ).accept( this, null );
			for ( int i = 1; i < typeArguments.size(); i++ ) {
				sb.append( ", " );
				typeArguments.get( i ).accept( this, null );
			}
			sb.append( '>' );
		}
		return null;
	}

	@Override
	public Object visitTypeVariable(TypeVariable t, Object o) {
		final Element typeVariableElement = t.asElement();
		if (typeVariableElement instanceof TypeParameterElement) {
			sb.append( typeVariableElement );
			if ( !"java.lang.Object".equals( t.getUpperBound().toString() ) && visitedTypeVariables.add( t ) ) {
				sb.append( " extends " );
				t.getUpperBound().accept( this, null );
				visitedTypeVariables.remove( t );
			}
		}
		else {
			typeVariableElement.asType().accept( this, null );
		}
		return null;
	}

	@Override
	public Object visitWildcard(WildcardType t, Object o) {
		sb.append( '?' );
		if ( t.getExtendsBound() != null ) {
			sb.append( " extends " );
			t.getExtendsBound().accept( this, null );
		}
		if ( t.getSuperBound() != null ) {
			sb.append( " super " );
			t.getSuperBound().accept( this, null );
		}
		return null;
	}

	@Override
	public Object visitUnion(UnionType t, Object o) {
		return null;
	}

	@Override
	public Object visitIntersection(IntersectionType t, Object o) {
		final List<? extends TypeMirror> bounds = t.getBounds();
		bounds.get( 0 ).accept( this, null );
		for (TypeMirror bound : bounds) {
			sb.append(" & ");
			bound.accept(this, null);
		}
		return null;
	}

	@Override
	public Object visitExecutable(ExecutableType t, Object o) {
		return null;
	}

	@Override
	public Object visitNoType(NoType t, Object o) {
		return null;
	}
}
