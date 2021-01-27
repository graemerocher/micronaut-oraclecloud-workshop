# Using the Micronaut HTTP Client

## Introduction
In this lab you will learn how to easily write HTTP clients in either a declarative or imperitive manner.

Estimated Lab Time: 20 minutes

### Objectives

In this lab you will:
* Using the Micronaut HTTP Client
* Non-Blocking Requests
* Service Discovery

### Prerequisites

## Declaring Client Interfaces

In the previous lab the tests you wrote invoked the `OwnerController` implementation directly and did not actually involve sending or receiving JSON over HTTP.

Micronaut features a built-in declarative HTTP client that simplifies the creation of HTTP clients. To try it out use this modified version of `OwnerControllerTest`.

    <copy>
    package example.micronaut;

    import io.micronaut.http.HttpStatus;
    import io.micronaut.http.annotation.Body;
    import io.micronaut.http.annotation.Get;
    import io.micronaut.http.annotation.Post;
    import io.micronaut.http.client.annotation.Client;
    import io.micronaut.http.client.exceptions.HttpClientResponseException;
    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import org.junit.jupiter.api.Test;

    import javax.inject.Inject;
    import java.util.Collection;

    import static org.junit.jupiter.api.Assertions.*;

    @MicronautTest
    public class OwnerControllerTest  {
        @Inject OwnerClient ownerClient;

        @Test
        void testAddOwnerInvalid() {
            HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () ->
                    ownerClient.add(new Owner("Bob", 10))
            );
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("owner.age: must be greater than or equal to 18", e.getMessage());

        }

        @Test
        void testAddOwnerValid() {
            Owner bob = ownerClient.add(new Owner("Bob", 35));
            assertNotNull(bob);
            assertEquals("Bob", bob.getName());
            assertEquals(35, bob.getAge());

            assertEquals(3, ownerClient.getOwners().size());
        }

        @Client("/owners")
        interface OwnerClient {
            @Get("/")
            Collection<Owner> getOwners();

            @Post("/")
            Owner add(@Body Owner owner);
        }
    }
    </copy>

The key part of this code is the `OwnerClient` interface which is defined as an inner class and then injected as a bean. By specifying a URI of `/owners` to the declarative client it is assumed you will be making requests to the current server.

> To debug the HTTP requests and responses try adding a logger definition like `<logger name="io.micronaut.http.client" level="trace" />` in `logback.xml`.

Micronaut will at compilation time produce an implementation of the `OwnerClient` interface which is injectable into your code. If you are interested to know how this works, see the [Introduction Advice](https://docs.micronaut.io/latest/guide/index.html#introductionAdvice) section of the Micronaut documentation.

## Non-Blocking Client Requests

It should be noted that in the example in the previous section the `OwnerClient` interface returns `Owner` and `Collection` directly, which means that Micronaut must block until the response is received from a client. This is fine for unit testing, but in production code it is far better to avoid blocking I/O where you can. To achieve this you can instead return Reactive types from the interface such as those defined by [RxJava](https://github.com/ReactiveX/RxJava).

Try the following example:

    <copy>
    package example.micronaut;

    import io.micronaut.http.HttpStatus;
    import io.micronaut.http.annotation.Body;
    import io.micronaut.http.annotation.Get;
    import io.micronaut.http.annotation.Post;
    import io.micronaut.http.client.annotation.Client;
    import io.micronaut.http.client.exceptions.HttpClientResponseException;
    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import io.reactivex.Flowable;
    import io.reactivex.Single;
    import org.junit.jupiter.api.Test;

    import javax.inject.Inject;

    import static org.junit.jupiter.api.Assertions.*;

    @MicronautTest
    public class OwnerControllerTest  {
        @Inject OwnerClient ownerClient;

        @Test
        void testAddOwnerInvalid() {
            HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () ->
                    ownerClient.add(new Owner("Bob", 10)).blockingGet()
            );
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("owner.age: must be greater than or equal to 18", e.getMessage());

        }

        @Test
        void testAddOwnerValid() {
            Owner bob = ownerClient.add(new Owner("Bob", 35)).blockingGet();
            assertNotNull(bob);
            assertEquals("Bob", bob.getName());
            assertEquals(35, bob.getAge());

            assertEquals(3, ownerClient.getOwners().toList().blockingGet().size());
        }

        @Client("/owners")
        interface OwnerClient {
            @Get("/")
            Flowable<Owner> getOwners();

            @Post("/")
            Single<Owner> add(@Body Owner owner);
        }
    }
    </copy>

Note that the test itself is still blocking to obtain the results, however in production code you can use [Reactive response processing](https://docs.micronaut.io/latest/guide/index.html#reactiveServer) to for example invoke another HTTP service and return a response without blocking at all.

## HTTP Services

When using HTTP clients in production for communication between Microservices you typically don't hard-code a URI to the current server such as `/owner`.

Micronaut features the notion of Service IDs to abstract the destination of a HTTP request. Service IDs form part of Micronaut's support for [Service Discovery](https://docs.micronaut.io/latest/guide/index.html#serviceDiscovery) including various strategies for discovering services, for example Consul and Kubernetes.

To demonstrate Service IDs in action modify the `OwnerClient` defined in the previous section as follows:

    <copy>
    @Client(id = "owners", path = "/owners")
    interface OwnerClient {
        @Get("/")
        Flowable<Owner> getOwners();

        @Post("/")
        Single<Owner> add(@Body Owner owner);
    }
    </copy>

Notice an `id` of `owners` is used, and the `path` attribute is used to include the root context to prefix all requests with.

Now modify `src/main/resources/application.yml` as follows:

    <copy>
    micronaut:
      application:
        name: demo
      http:
        services:
          owners:
            urls: http://localhost:8080
    owners:
      fred:
        name: Fred
        age: 35
      barney:
        name: Barney
        age: 30
    </copy>

The above configures Micronaut to look for services with an ID of `owners` at the given address or addresses (if there are multiple Micronaut will client-side load balance between them) using the `micronaut.http.services.owners.url` property where `owners` is the Service ID.

Now ensure your application is _not_ running and run `OwnerControllerTest`, which will fail with errors like the following:

```
13:57:03.693 [Test worker] ERROR i.m.r.intercept.RecoveryInterceptor - Type [example.micronaut.OwnerControllerTest$OwnerClient$Intercepted] executed with error: Connect Error: Connection refused: localhost/127.0.0.1:8080
io.micronaut.http.client.exceptions.HttpClientException: Connect Error: Connection refused: localhost/127.0.0.1:8080
    at io.micronaut.http.client.netty.DefaultHttpClient.lambda$null$33(DefaultHttpClient.java:1111)
```

This is expected because nothing is running on localhost on port 8080. Now start the server by running the `Application` class as explained in Lab 1 and then run the `OwnerControllerTest` again.

The test passes! You can also modify `application.yml` to include multiple destination servers:

```
micronaut:
  application:
    name: demo
  http:
    services:
      owners:
        urls:
          - http://localhost:8080
          - http://localhost:8085
```

> It is also important to remember that Micronaut can be configured with the environment so you could define an environment variable called `MICRONAUT_HTTP_SERVICES_OWNERS_URLS` and provide a comma-separated list of URLs to connect to

Of course there are many ways in which services can be discovered by ID that differ depending your deployment architecture. See the documentation on [Service Discovery](https://docs.micronaut.io/latest/guide/index.html#serviceDiscovery) to see the possibilities for your cloud.

## Cleanup Changes

Before you go make sure to cleanup the changes by restoring your `application.yml` to the following:

    <copy>
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
    </copy>

and reverting the changes to `OwnerClient`:

    <copy>
    @Client("/owners")
    interface OwnerClient {
        @Get("/")
        Flowable<Owner> getOwners();

        @Post("/")
        Single<Owner> add(@Body Owner owner);
    }
    </copy>

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar
