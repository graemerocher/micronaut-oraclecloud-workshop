# Writing Web Controllers with Micronaut

## Introduction
In this you will learn how to write a REST API that produces JSON output.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* How to write POJOs that define your JSON responses
* How to write controller routes that match requests
* How to return responses from the controller

### Prerequisites

## Define a Controller

Micronaut's built in HTTP server is based on the Netty I/O toolkit in combination with an annotation-based programming model for defining routes with built in support for emitting and consuming JSON via the Jackson library.

> Note that Micronaut does include optional additional modules that add support for things like [Server-side view rendering with template engines like Thymleaf and Velocity](https://micronaut-projects.github.io/micronaut-views/latest/guide/) and [XML responses](https://micronaut-projects.github.io/micronaut-jackson-xml/latest/guide/index.html).

The annotation-based programming model should be very familiar to anyone who has used libraries like Spring MVC or JAX-RS (note if you prefer those annotation models it is possible to use [JAX-RS annotations](https://micronaut-projects.github.io/micronaut-jaxrs/latest/guide/index.html) and [Spring annotations](https://micronaut-projects.github.io/micronaut-spring/latest/guide/) with Micronaut as well).

To get started with an example create a file called `src/main/java/example/micronaut/OwnerController.java` and populate it with the following contents:

```
package example.micronaut;

import io.micronaut.http.annotation.Controller;

@Controller("/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }
}
```

As you can see the `OwnerController` defines a constructor that injects the `OwnerService` and on the class the `@Controller` annotation is used to define the root URI to this controller.


## Specify Routes

To expose an individual route over HTTP you need to define methods that are annotated with an applicable annotation for each HTTP method you wish to expose. Try add the following definition:

```
    @Get("/")
    List<Owner> getOwners() {
        return ownerService.getInitialOwners();
    }
```

This uses the `io.micronaut.http.annotation.Get` annotation to indicate that HTTP get requests to the root URI under `/owners` should match this method and invoke it. The return type indicates the response that will be sent over HTTP which by default is assumed to be JSON.


## Return JSON Responses

As mentioned the default response content type is JSON. To see that in action configure some initial owners by modifying your `src/main/resources/application.yml` file so that 2 `OwnerConfiguration` beans are created:

```
micronaut:
  application:
    name: demo
owners:
  fred:
    name: Fred
    age: 35
  barney:
    name: Barney
    age: 30
```

Now run your `Application` class from the IDE (as described in lab 1) and open up Terminal from the IDE and run `curl` to see your response:

```
curl -i http://localhost:8080/owners
HTTP/1.1 200 OK
Date: Thu, 26 Nov 2020 08:27:30 GMT
Content-Type: application/json
content-length: 53
connection: keep-alive

[{"name":"Barney","age":30},{"name":"Fred","age":35}]
```

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar