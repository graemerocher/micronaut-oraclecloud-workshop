# Integration Testing with Micronaut

## Introduction
In this lab you will learn how to work with integration testing in Micronaut.

Estimated Lab Time: 15 minutes

### Objectives

In this lab you will:
* Learn how to define tests with `@MicronautTest`
* Understand how beans can be mocked

### Prerequisites

## Using the @MicronautTest Annotation

So far during the Hands-on Lab you have been issuing `curl` requests from Terminal to try out the API, however a far better way to test your API is to write integration tests.

The [Micronaut Test](https://micronaut-projects.github.io/micronaut-test/latest/guide/) module makes writing integration tests a breeze by allowing you to define the `@MicronautTest` annotation on any test. This has the effect of bootstrapping Micronaut and letting you dependency inject beans into the test itself.

To test this out create a new test in the file `src/test/java/example/micronaut/OwnerControllerTest.java` with the following contents:

    <copy>
    package example.micronaut;

    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import static org.junit.jupiter.api.Assertions.*;
    import org.junit.jupiter.api.Test;
    import javax.inject.Inject;
    import javax.validation.ConstraintViolationException;

    @MicronautTest
    public class OwnerControllerTest {
        @Inject OwnerController ownerController;

        @Test
        void testAddOwnerInvalid() {
            assertThrows(ConstraintViolationException.class, () ->
                    ownerController.add(new Owner("Bob", 10))
            );
        }

        @Test
        void testAddOwnerValid() {
            Owner bob = ownerController.add(new Owner("Bob", 35));
            assertNotNull(bob);
            assertEquals("Bob", bob.getName());
            assertEquals(35, bob.getAge());
            assertTrue(ownerController.getOwners().contains(bob));
        }
    }
    </copy>

The `OwnerControllerTest` injects the `OwnerController` and checks our validation logic is working as anticipated. Note this is running your real production code and is not mocking anything.

If you are using Gradle and receive an error for a `ParameterResolutionException` during test execution, then add the following to your `build.gradle`:

    <copy>
    test {
        exclude '**/*Test$Intercepted*'
    }
    </copy>

## Supplying Configuration to a Test

Sometimes you need to test the different ways in which your application can be configured. One way to do this is to create a file called `src/main/resources/application-test.yml`.

When you run tests Micronaut automatically activates the `test` environment which causes the above file to be loaded (see Lab 2 where environments were covered).

If your requirements are more dynamic however there are other options. One is the [@Property](https://docs.micronaut.io/latest/api/io/micronaut/context/annotation/Property.html) annotation. To try this out modify `OwnerServiceTest` adding `@MicronautTest` and `@Property` to feed the initial user list:

    <copy>
    package example.micronaut;

    import io.micronaut.context.annotation.Property;
    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import org.junit.jupiter.api.Test;

    import javax.inject.Inject;
    import java.util.Collection;
    import static org.junit.jupiter.api.Assertions.*;

    @MicronautTest
    @Property(name = "owners.bob.name", value = "Bob")
    @Property(name = "owners.bob.age", value = "25")
    public class OwnerServiceTest {
        @Inject OwnerService ownerService;

        @Test
        void testOwners() {
            Collection<Owner> initialOwners = ownerService.getInitialOwners();
            assertEquals(
                    3,
                    initialOwners.size()
            );

            assertTrue(
                    initialOwners.stream().anyMatch(o -> o.getName().equals("Bob"))
            );
        }
    }
    </copy>

In this example `@Property` is used to add an additional initial owner to the collection when the application starts up.

`TestPropertyProvider` is an alternative to this approach which allows programmatic configuration to be fed to the application. Try modifying the test code as follows:

    <copy>
    package example.micronaut;

    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import io.micronaut.test.support.TestPropertyProvider;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.TestInstance;

    import javax.inject.Inject;
    import java.util.Collection;
    import java.util.Map;

    import static org.junit.jupiter.api.Assertions.*;

    @MicronautTest
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class OwnerServiceTest implements TestPropertyProvider {
        @Inject OwnerService ownerService;

        @Override
        public Map<String, String> getProperties() {
            return Map.of(
                    "owners.bob.name", "Bob",
                    "owners.bob.age", "25"
            );
        }

        @Test
        void testOwners() {
            Collection<Owner> initialOwners = ownerService.getInitialOwners();
            assertEquals(
                    3,
                    initialOwners.size()
            );

            assertTrue(
                    initialOwners.stream().anyMatch(o -> o.getName().equals("Bob"))
            );
        }
    }
    </copy>

In this case the `getProperties()` method is used to customize configuration. Note however that you must annotate the class with `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` for JUnit 5 tests for this approach to work.

## Mocking Beans

It is a common requirement to mock parts of the application, in particular those that need to do network I/O or access a potentially unavailable remote resource.

To aid mocking and make your application more extensible it is good practise to use interfaces as much as possible. To demonstrate this extract the methods from the `OwnerService` into a new interface in a file called `src/main/java/example/micronaut/OwnerOperations.java`:

    <copy>
    package example.micronaut;

    import java.util.Collection;

    public interface OwnerOperations {
        @Logged
        Collection<Owner> getInitialOwners();

        void addOwner(Owner owner);
    }
    </copy>

> Note that you can apply AOP advice on the interfaces

Now modify `OwnerService` to implement this interface:

    <copy>
    @Singleton
    public class OwnerService implements OwnerOperations {
        // remaining code omitted for brevity
    }
    </copy>

Next modify `OwnerController` to inject the interface you have defined instead of the implementation:

    <copy>
    package example.micronaut;

    import io.micronaut.http.annotation.Body;
    import io.micronaut.http.annotation.Controller;
    import io.micronaut.http.annotation.Get;
    import io.micronaut.http.annotation.Post;

    import javax.validation.Valid;
    import java.util.Collection;

    @Controller("/owners")
    public class OwnerController {
        private final OwnerOperations ownerOperations;

        public OwnerController(OwnerOperations ownerOperations) {
            this.ownerOperations = ownerOperations;
        }

        @Get("/")
        Collection<Owner> getOwners() {
            return ownerOperations.getInitialOwners();
        }

        @Post("/")
        Owner add(@Valid @Body Owner owner) {
            ownerOperations.addOwner(owner);
            return owner;
        }
    }
    </copy>

Now if you wish to supply a mock version of `OwnerOperations` you can use the `@MockBean` annotation on a method of your test. 

The `@MockBean` annotation allows you to replace an injectable bean for only the scope of the executing test using a method of the test itself. 

> **TIP**: For more information on testing with mocks and Micronaut see the documentation for [Micronaut Test](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html#junit5)

In the following altered version of `OwnerControllerTest` a new method called `ownerOperations` is added that replaces the the default implementation of `OwnerOperations` (in this case `OwnerService`) with and implementation provided by the test itself:

    <copy>
    package example.micronaut;

    import io.micronaut.test.annotation.MockBean;
    import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
    import org.junit.jupiter.api.Test;
    import javax.inject.Inject;
    import javax.validation.ConstraintViolationException;
    import java.util.ArrayList;
    import java.util.Collection;
    import static org.junit.jupiter.api.Assertions.*;

    @MicronautTest
    public class OwnerControllerTest implements OwnerOperations {

        @Inject OwnerController ownerController;

        private final Collection<Owner> owners = new ArrayList<>();

        @Test
        void testAddOwnerInvalid() {
            assertThrows(ConstraintViolationException.class, () ->
                    ownerController.add(new Owner("Bob", 10))
            );
        }

        @Test
        void testAddOwnerValid() {
            Owner bob = ownerController.add(new Owner("Bob", 35));
            assertNotNull(bob);
            assertEquals("Bob", bob.getName());
            assertEquals(35, bob.getAge());
            assertTrue(owners.contains(bob));
        }

        @MockBean(OwnerOperations.class)
        OwnerOperations ownerOperations() {
            return this;
        }

        @Override
        public Collection<Owner> getInitialOwners() {
            return owners;
        }

        @Override
        public void addOwner(Owner owner) {
            owners.add(owner);
        }
    }
    </copy>

Note if you don't want to implement the whole interface yourself you can instead consider returning a Mock object using a framework like [Mokito](https://site.mockito.org).

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar
