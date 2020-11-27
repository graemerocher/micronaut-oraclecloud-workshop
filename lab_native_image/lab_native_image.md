# Building Native Images with Micronaut & GraalVM

## Introduction
In this lab you will learn how to turn your application into a native executable with GraalVM Native Image.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Understand the benefits and tradeoffs with AOT compilation
* Build a native executable from your Micronaut application
* Run the executable on a VM or with Docker

### Prerequisites
- Access to your project instance

## Overview of Native Image

[GraalVM Native Image](https://www.graalvm.org/reference-manual/native-image/) is a technology that allows performing a closed world static analysis of a Java application and turn the Java application into a native executable designed to execute on a specific target environment.

Whilst building a native image can take some time the benefits include a dramatic reduction in startup time and reduced overall memory consumption both of which can significantly reduce the cost of running Cloud applications over time.

## Building a Native Image with Gradle

If you are using Gradle and the GraalVM SDK with Native Image installed (Native Image is an optional component installable via `gu install native-image`) then building a native image is trivial.

Open up the Terminal pane and run the following command:

	<copy>
	./gradlew nativeImage
	</copy>

After some time the native image executable with be built to `build/native-image/application`.

You can now run the native executable from Terminal:

	<copy>
	./build/native-image/application
	</copy>

## Building a Native Image with Maven

If instead you are using Maven then a slightly different command is needed. Try run:

<copy>
./mvnw package -Dpackaging=native-image
</copy>

Which will build the native image into the `target/native-image` directory which you can run with:

	<copy>
	./target/native-image/application
	</copy>


You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar