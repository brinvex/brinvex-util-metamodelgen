package com.brinvex.util.metamodelgen.test;

import com.brinvex.util.metamodelgen.annotations.GeneratePropNamesMetamodel;

@GeneratePropNamesMetamodel
public class Person {

    private Integer yearOfBirth;

    private String fullName;

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
