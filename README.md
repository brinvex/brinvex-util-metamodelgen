# Brinvex-Util-Metamodelgen

### Introduction

Brinvex-Util-Metamodelgen is a compact Java library which enables developers
to easily generate metamodel classes having static fields which represent instance fields of underlying class.

### Maven dependency declaration
````
<repository>
    <id>brinvex-mvn-repo-public</id>
    <url>https://github.com/brinvex/brinvex-mvn-repo-public/raw/main/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-metamodelgen-annotations</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-metamodelgen-processor</artifactId>
    <scope>provided</scope>
    <version>1.0.0</version>
    <optional>true</optional>
</dependency>
````
### Example

Put ``@GeneratePropNamesMetamodel`` on your some POJO
````java
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
````
Rebuild your project, and you should see generated metamodel class in the ``target/generated-sources/annotations`` directory: 
````java
@Generated(value = "com.brinvex.util.metamodelgen.processor.MetamodelGenerator")
public abstract class Person_ {

    public static final String fullName = "fullName";
    public static final String yearOfBirth = "yearOfBirth";

}
````

### Requirements
- Java 17 or above

### License

- The _Brinvex-Util-Metamodelgen_ is released under version 2.0 of the Apache License.
