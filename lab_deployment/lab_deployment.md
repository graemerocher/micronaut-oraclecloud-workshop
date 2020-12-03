# Deploying Micronaut Applications to the Cloud

## Introduction
In this lab you will learn how to deploy your Micronaut application to a VM, a Docker Container Registry and Kubernetes.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Learn how to build an executable JAR file that can be deployed to any VM
* Understand how to build and deploy Docker images to Oracle Container Registry
* Learn how to deploy a Micronaut application to Kubernetes

### Prerequisites

- Access to your project instance

## Deploying a Runnable JAR to a VM

To deploy the application as a Runnable JAR open up Terminal (ALT + F12) and run the following command in Gradle:

	<copy>
	./gradlew assemble
	</copy>

Or with Maven:

	<copy>
	./mvnw package
	</copy>

A runnable JAR file will be built that is ready to be executed in production on a VM. 

To run the JAR file if you built with Gradle run:

	<copy>
	java -jar build/libs/example-0.1-all.jar
	</copy>

> NOTE: Make sure you run the JAR that ends with `-all`!

Of if you used Maven:

<copy>

</copy>

## Deploying a Container to Oracle Container Registry

TODO 

## Deploying a Container to Container Engine for Kubernetes (OKE)

TODO 

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar