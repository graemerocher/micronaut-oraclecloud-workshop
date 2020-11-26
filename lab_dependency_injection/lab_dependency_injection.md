# Using Dependency Injection in Micronaut

## Introduction
In this lab you will learn how Dependency Injection forms the foundation of how Micronaut works.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Learn how to define Beans
* Understand how to inject one bean to another
* Learn the benefits of loose coupling

### Prerequisites
- Access to your project instance

## Defining Managed Beans

Micronaut is fundamentally based on Dependency Injection (DI) techniques that have been used in Java applications for many years in frameworks like Spring and CDI.

The primary difference is that Micronaut will at compilation time compute the injection rules necessary to wire together your application.

In order for Micronaut to do that you need designate which classes in your application are managed "beans".

This is done by defining an annotation on the class that itself is annotated with `javax.inject.Scope`. 

A scope is defines the lifecycle of a bean and aspects such as how many instances are allowed. The most common scope is `javax.inject.Singleton` which indicates that at most 1 instance is allowed of the object. 

For more information on other available scopes see the [Scopes](https://docs.micronaut.io/latest/guide/index.html#scopes) section of the Micronaut documentation.

Try create a bean in a file called `src/main/java/example/micronaut/OwnerService.java`:


```java
package example.micronaut;

import javax.inject.Singleton;

@Singleton
public class OwnerService {

}
```

The `OwnerService` class is annotated with `@Singleton` which means it is now managed by Micronaut and available as a bean to be injected into other objects.

To demonstrate this define a test in `src/test/java/example/micronaut/OwnerServiceTest.java`:

```java

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OwnerServiceTest {
    
    @Test
    void testOwnerService() {
        try (ApplicationContext context = ApplicationContext.run()) {
            OwnerService ownerService = context.getBean(OwnerService.class);
            assertNotNull(ownerService);
        }
    }
}
```

Here you can see the test uses Micronaut's `ApplicationContext`, which is a container object that manages all beans, to lookup an instance of `OwnerService`. Whilst this example is not particularly interesting, if you invoke `getBean` multiple times you will see that the instances are the same:


	@Test
	void testOwnerService() {
	    try (ApplicationContext context = ApplicationContext.run()) {
	        OwnerService ownerService = context.getBean(OwnerService.class);
	        assertEquals(
	        	ownerService,
				context.getBean(OwnerService.class)
	    	);
	    }
	}

The `@Singleton` scope ensures that only one instance is created.

## Add Lifecycle Methods

Life cycle methods can be added via `javax.annotation.PostConstruct` and `javax.annotation.PreDestroy` annotations. Try adding the following methods to `OwnerService`:

```
    @PostConstruct
    void created() {
        System.out.println("OwnerService created");
    }

    @PreDestroy
    void destroyed() {
        System.out.println("OwnerService destroyed");
    }
``` 

Now run the test in the previous section and you will see the following output:

```
OwnerService created
OwnerService destroyed
```

This is because the `ApplicationContext` is created and destroyed in the `try-with-resources` block so when the bean is created it is triggers the `created` method and when the context is destroyed it triggers the `destroyed` method.

## Injecting Beans 

To demonstrating dependency injection better, let's tackle a more interesting case. First define a class POJO that is going to represent the owners of a pets in a hypothetical petclinic in a file called `src/main/java/example/micronaut/Owner.java`:

```
package example.micronaut;

public class Owner {
    private final String name;
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
```

Now let's setup some logic that let's you configure an initial set of owners using  configuration injection to resolve values from the environment in a file called `src/main/java/example/micronaut/OwnerConfiguration.java`:

```
package example.micronaut;

import io.micronaut.context.annotation.EachProperty;

@EachProperty("owners")
public class OwnerConfiguration {
    private String name;
    private int age;

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

```

This class uses `@EachProperty` which indicates for every property passed to Micronaut that starts with "owners" a new bean of type `OwnerConfiguration` will be created.

With that in place you can inject all of the `OwnerConfiguration` instances into the constructor of the `OwnerService`:

```
package example.micronaut;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OwnerService {
    private final List<OwnerConfiguration> ownerConfiguration;

    public OwnerService(List<OwnerConfiguration> ownerConfiguration) {
        this.ownerConfiguration = ownerConfiguration;
    }

    List<Owner> getInitialOwners() {
        return ownerConfiguration.stream()
                 .map(OwnerConfiguration::create)
                .collect(Collectors.toList());
    }
}
```

Micronaut will automatically lookup and populate the available `OwnerConfiguration` instances using constructor injection. You can also alternatively use field injection in the form:

```
@javax.inject.Inject List<OwnerConfiguration> ownerConfiguration;
```

However, constructor injection is prefered as it encourages immutability and more clearly expresses the requirements of the class.


So how do you make the `OwnerConfiguration` instances available? Try adding the following test to the `OwnerServiceTest` you created early:

```
    @Test
    void testOwners() {
        Map<String, Object> configuration = Map.of(
                "owners.fred.name", "Fred",
                "owners.fred.age", "35",
                "owners.barney.name", "Barney",
                "owners.barney.age", "30"
        );
        try (ApplicationContext context = ApplicationContext.run(configuration)) {
            OwnerService ownerService = context.getBean(OwnerService.class);
            List<Owner> initialOwners = ownerService.getInitialOwners();
            assertEquals(
                    2,
                    initialOwners.size()
            );

            assertTrue(
                    initialOwners.stream().anyMatch(o -> o.getName().equals("Fred"))
            );
        }
    }
```

Notice that for each entry under the `owner` configuration namespace you get a new instance of `OwnerConfiguration` thanks to how `@EachProperty` works. Also notice how you can pass configuration to the `run` method of the `ApplicationContext` in order to configure your application.

You can additionally lookup an individual named instance used the `@javax.inject.Named(..)` qualifier, for example:


    public OwnerService(@Named("fred") OwnerConfiguration> fredConfiguration) {
        this.ownerConfiguration = fredConfiguration;
    }


You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar
