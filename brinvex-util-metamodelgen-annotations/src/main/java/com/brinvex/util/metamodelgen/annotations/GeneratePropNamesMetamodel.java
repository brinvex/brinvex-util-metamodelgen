package com.brinvex.util.metamodelgen.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Documented
@Target(TYPE)
@Retention(value = SOURCE)
public @interface GeneratePropNamesMetamodel {
}
