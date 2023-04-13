package com.brinvex.util.metamodelgen.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class PersonTest {

    @Test
    void metamodelFields() {
        Field[] fields = Person.class.getDeclaredFields();
        Field[] metaFields = Person_.class.getFields();

        Assertions.assertEquals(fields.length, metaFields.length);

    }
}
