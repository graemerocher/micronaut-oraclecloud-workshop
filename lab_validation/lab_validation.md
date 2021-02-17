# Validating Input with Micronaut Validation

## Introduction
In this lab you will learn how to ensure incoming data is validated.

Estimated Lab Time: 15 minutes

### Objectives

In this lab you will:
* Learn how to define validation constraints on methods
* Learn how to modify POJOs to include validation constraints
* Understand how to handle errors

### Prerequisites
- Access to your project instance

## Validate Configuration

Validation via `javax.validation` is integrated directly into Micronaut across all layers of your application. One common area you may want to validate is application configuration.

Try adding some validation constraints to the fields of the `OwnerConfiguration` class:

    <copy>
    package example.micronaut;

    import io.micronaut.context.annotation.Context;
    import io.micronaut.context.annotation.EachProperty;

    import javax.validation.constraints.Min;
    import javax.validation.constraints.NotBlank;

    @EachProperty("owners")
    @Context
    public class OwnerConfiguration {

        @NotBlank
        private String name;

        @Min(18)
        private int age;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        Owner create() {
            return new Owner(name, age);
        }
    }
    </copy>

In this example the `@NotBlank` annotation ensures the name cannot be blank and the `@Min` annotation ensures owners must be at least 18 years of age.

Notice as well the addition of the [@Context](https://docs.micronaut.io/latest/api/io/micronaut/context/annotation/Context.html) annotation, which ensures that any configurations are bound to the lifecycle of the `ApplicationContext` such that they are created on startup instead of being initialized lazily. This ensures that validation rules are executed on application startup.

Try modify the `testOwners` test now in `OwnerServiceTest` and pass in invalid data such as an `age` that violates the previously defined constraint:

    <copy>
    @Test
    void testOwners() {
        Map<String, Object> configuration = Map.of(
                "owners.fred.name", "Fred",
                "owners.fred.age", "10",
                "owners.barney.name", "Barney",
                "owners.barney.age", "30"
        );
        ...
    }
    </copy>

Then run the test and you will see an error such as the following:

```
Bean definition [example.micronaut.OwnerConfiguration] could not be loaded: Error instantiating bean of type  [example.micronaut.OwnerConfiguration]

Message: Validation failed for bean definition [example.micronaut.OwnerConfiguration]
List of constraint violations:[
    age - must be greater than or equal to 18
]
```

## Validate Controller Input

In addition to validating configuration, a common use case for validation is accepting user input via an HTTP API.

To demonstrate this first modify the `OwnerService` to store users in an in-memory map for the moment:

    <copy>
    package example.micronaut;

    import javax.inject.Singleton;
    import java.util.Collection;
    import java.util.List;
    import java.util.concurrent.ConcurrentLinkedDeque;

    @Singleton
    public class OwnerService {
        private final Collection<Owner> owners = new ConcurrentLinkedDeque<>();

        public OwnerService(List<OwnerConfiguration> ownerConfigurations) {
            for (OwnerConfiguration configuration : ownerConfigurations) {
                Owner owner = configuration.create();
                owners.add(owner);
            }
        }

        @Logged
        public Collection<Owner> getInitialOwners() {
            return owners;
        }

        public void addOwner(Owner owner) {
            owners.add(owner);
        }
    }
    </copy>

The `Owner` service now manages a mutable collection of `Owner` instances initialized from configuration. The `addOwner` method can be used to add a new owner.

Now add a new route to the `OwnerController` that handles POST requests and allows adding new `Owner` instances:

    <copy>
    @Post("/")
    Owner add(@NotBlank String name, @Min(18) int age) {
        Owner owner = new Owner(name, age);
        ownerService.addOwner(owner);
        return owner;
    }
    </copy>

You'll also need these imports:

    <copy>
    import io.micronaut.http.annotation.Post;
    import javax.validation.constraints.Min;
    import javax.validation.constraints.NotBlank;
    </copy>

The `add` method takes two arguments, the `Owner` `name` and `age`, and uses validation constraints on the parameters to ensure only valid arguments are supplied. The valid owner instance is constructed and passed to the `OwnerService`'s `addOwner` method.

Try running your application again as described in Lab 1 and then run the following `curl` command from Terminal, again supplying an invalid `age` value:

```
$ curl -H "Content-Type:application/json" -X POST -i http://localhost:8080/owners -d '{"name":"Fred", "age":10}'

HTTP/1.1 400 Bad Request
Content-Type: application/json
content-length: 109
connection: keep-alive

{"message":"age: must be greater than or equal to 18","_links":{"self":{"href":"/owners","templated":false}}}
```

As you can see the API returned an error indicating the `age` passed in the JSON body is below the required minimum value of 18.

## Validating POJOs

Using individual parameters of a controller method is one way to define validation rules, however you can also define them on `@Introspected` classes directly.

Try modifying the `Owner` class with the validation constraints such as the below:

    <copy>
    package example.micronaut;

    import io.micronaut.core.annotation.Introspected;

    import javax.validation.constraints.Min;
    import javax.validation.constraints.NotBlank;

    @Introspected
    public class Owner {

        @NotBlank
        private final String name;

        @Min(18)
        private final int age;

        public Owner(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
    </copy>

In this case the constraints are defined directly on the fields of the `Owner` class.

Now modify the `add` method of the `OwnerController` to instead validate an instance of `Owner`:

    <copy>
    @Post("/")
    Owner add(@Valid @Body Owner owner) {
        ownerService.addOwner(owner);
        return owner;
    }
    </copy>

You'll also need these imports:

    <copy>
    import io.micronaut.http.annotation.Body;
    import javax.validation.Valid;
    </copy>

In this case the `javax.validation.Valid` annotation is used to indicate that only a valid instance of `Owner` is accepted. Note that the `io.micronaut.http.annotation.Body` annotation is used to indicate that the whole body should be bound to the `Owner` parameter.

Now you can repeat the same `curl` command from the previous section and the same error will result.

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar
