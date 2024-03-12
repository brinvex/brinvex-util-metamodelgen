# Brinvex-Util-Metamodelgen

### Introduction

Brinvex-Util-Metamodelgen is a compact Java library which enables developers
to easily generate metamodel classes having static fields which represent instance fields of underlying class.

### Maven configuration
````
<properties>
    <brinvex-util-metamodelgen.version>1.0.0</brinvex-util-metamodelgen.version>
</properties>
````
````
<repository>
    <id>brinvex-repo</id>
    <name>Brinvex Repository</name>
    <url>https://github.com/brinvex/brinvex-repo/raw/main/</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>
````
````
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-metamodelgen-annotations</artifactId>
    <version>${brinvex-util-metamodelgen.version}</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
````
````
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.brinvex.util</groupId>
                        <artifactId>brinvex-util-metamodelgen-processor</artifactId>
                        <version>${brinvex-util-metamodelgen.version}</version>
                    </path>
                </annotationProcessorPaths>
                <annotationProcessors>
                    <annotationProcessor>com.brinvex.util.metamodelgen.processor.MetamodelGenerator</annotationProcessor>
                </annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>

````
### Example

Put ``@GeneratePropNamesMetamodel`` on some POJO
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
