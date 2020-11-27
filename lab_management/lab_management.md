# Enabling Management Features in Micronaut

## Introduction
In this lab you will learn how to enable management features in Micronaut including health checks and metrics.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Learn how to configure the Micronaut management module
* Understand how to enable and expose management endpoints
* Define your own health checks

### Prerequisites

- Access to your project instance

## Enabling Management Endpoints

In a Microservice architecture monitoring and visibility to application health is critical and to make this easier Micronaut features built-in capabilities to manage the health of your application.

To get started modify your Gradle build by adding the following dependencies to your `build.gradle` file within the `dependencies` block:

	<copy>
	implementation("io.micronaut:micronaut-management")
	</copy>

Or if you are using Maven first add the `swagger-annotations` dependency under `<dependencies>`:

	<copy>
	<dependency>
		<groupId>io.micronaut</groupId>
		<artifactId>micronaut-management</artifactId>
		<scope>compile</scope>
	</dependency>
	</copy>	

Before proceeding you should refresh your project dependencies:

![Project Dialog](../images/dependency-refresh.png)	

Now add the following configuration to your `src/main/resources/application.yml` file:

<copy>
endpoints:
  all:
    enabled: true
    sensitive: false
  health:
    details-visible: anonymous
</copy>

Note that Micronaut is very conservative about exposing management endpoints that could leak information and result in a security vulnerability so the majority of endpoints default to `sensitive: true`.

The above configuration will set all endpoints to not be sensitive and also allow anonymous access to the `/health` endpoint. 

In a real world application you would either secure your application with [Micronaut Security](https://micronaut-projects.github.io/micronaut-security/latest/guide/) or set `endpoints.all.port` to a different port that is not exposed over the open web.

Now run your application and then open up terminal and issue a request to `http://localhost:8080/health`:

TODO: replace with output from Oracle Cloud

```
curl -i http://localhost:8080/health
HTTP/1.1 200 Ok
Date: Fri, 27 Nov 2020 11:10:14 GMT
Content-Type: application/json
content-length: 416
connection: keep-alive

{"name":"demo","status":"UP","details":{"jdbc":{"name":"demo","status":"UP","details":{"jdbc:h2:mem:devDb":{"name":"demo","status":"UP","details":{"database":"H2","version":"1.4.199 (2019-03-13)"}}}},"compositeDiscoveryClient()":{"name":"demo","status":"UP"},"diskSpace":{"name":"demo","status":"UP","details":{"total":1000240963584,"free":35355635712,"threshold":10485760}},"service":{"name":"demo","status":"UP"}}}%
```	

As you can see a HTTP 200 response is returned and a status of `UP` indicated. There are various built in health checks that monitor diskspace and the database health. 

## Defining Health Checks

You can define additional project specific [HealthIndicator](https://docs.micronaut.io/latest/api/io/micronaut/management/health/indicator/HealthIndicator.html) to extend the functionality of application health checking.

To try this out add two new methods to the `PetRepository` interface:

	<copy>
	io.reactivex.Single<Boolean> existsByHealth(Pet.PetHealth health);

	void updatePet(@Id Long id, Pet.PetHealth health);
	</copy>

The first checks if any `Pet` instances exist that correspond the given `PetHealth` enum and the second we will use for testing which allows you to update a `Pet` instances `PetHealth` by ID.

Now create a new file called `src/main/java/example/micronaut/PetHealthIndicator.java` and define a class that implements the `HealthIndicator` interface:

	<copy>
	package example.micronaut;

	import io.micronaut.health.HealthStatus;
	import io.micronaut.management.health.indicator.HealthIndicator;
	import io.micronaut.management.health.indicator.HealthResult;
	import io.reactivex.Flowable;
	import org.reactivestreams.Publisher;

	import javax.inject.Singleton;
	import java.util.Collections;

	@Singleton
	public class PetHealthIndicator implements HealthIndicator {
	    private final PetRepository petRepository;

	    public PetHealthIndicator(PetRepository petRepository) {
	        this.petRepository = petRepository;
	    }

	    @Override
	    public Publisher<HealthResult> getResult() {
	        return petRepository.existsByHealth(Pet.PetHealth.REQUIRES_VACCINATION)
	                .flatMapPublisher((petsNeedVaccine) -> {
	                    String message = petsNeedVaccine ? "Pets Need Vaccine" : "All Pets Vaccinated";
	                    HealthResult result = HealthResult.builder("pets")
	                            .status(petsNeedVaccine ? HealthStatus.DOWN : HealthStatus.UP)
	                            .details(Collections.singletonMap(
	                                    "description", message
	                            )).build();
	                    return Flowable.just(result);
	                });
	    }
	}
	</copy>

The `HealthIndicator` expects a `Publisher` as a result and for the indicator to be implemented in a non-blocking manner. The `existsByHealth` method you defined earlier uses an RxJava `Single` that will automatically execute on the I/O thread pool and if a `Pet` needs a vaccine a `HealthStatus` of `DOWN` will be returned, otherwise `UP` will be returned meaning that an instance of the application becomes unhealthy if any Pet instances require a vaccine.	

It's time to write a test! Create a new file called `src/main/test/example/micronaut/PetHealthIndicatorTest.java` and populate it with the following contents:

	<copy>
	package example.micronaut;

	import io.micronaut.http.HttpResponse;
	import io.micronaut.http.HttpStatus;
	import io.micronaut.http.client.HttpClient;
	import io.micronaut.http.client.annotation.Client;
	import io.micronaut.http.client.exceptions.HttpClientResponseException;
	import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
	import org.junit.jupiter.api.Test;
	import javax.inject.Inject;

	import static org.junit.jupiter.api.Assertions.assertEquals;
	import static org.junit.jupiter.api.Assertions.assertThrows;

	@MicronautTest(transactional = false)
	public class PetHealthIndicatorTest {

	    @Inject @Client("/")
	    HttpClient httpClient;

	    @Inject
	    PetRepository petRepository;

	    @Test
	    void testPetHealth() {
	        HttpResponse<?> response = httpClient.toBlocking().exchange("/health");
	        assertEquals(HttpStatus.OK, response.status());

	        Pet pet = petRepository.findByNameAndOwnerName("Hoppy", "Barney");

	        petRepository.updatePet(
	                pet.getId(),
	                Pet.PetHealth.REQUIRES_VACCINATION
	        );

	        response  = assertThrows(HttpClientResponseException.class, () ->
	                httpClient.toBlocking().exchange("/health")
	        ).getResponse();
	        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.status());
	    }
	}
	</copy>

There are some important aspects to note about this test:

1. By default `@MicronautTest` will rollback transactions between each test execution to ensure data cleanup occurs. `@MicronautTest(transactional = false)` is used to disable transaction rollback between test executions otherwise the update operation would only be visible to the test execution and not to the invoked HTTP server. 
2. The Micronaut `HttpClient` is used to check the response status is OK initially
3. Then a `Pet` is updated with the `updatePet` method setting the `PetHealth` to `REQUIRES_VACCINATION`
4. Then `assertThrows` is used to verify that `SERVICE_UNAVAILABLE` is returned from the `/health` endpoint


## Exposing Prometheus Metrics

Exposing application metrics and data points about the behaviour of your application in a Microservice architecture can be critical to maintaining production systems.

Micronaut has support for the [Micrometer](https://micrometer.io) library for exporting application metrics which supports pluggable meter registries.

To get started modify your Gradle build by adding the following dependencies to your `build.gradle` file within the `dependencies` block:

	<copy>	
	runtimeOnly("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
	</copy>

Or if you are using Maven first add the `micronaut-micrometer-registry-prometheus` dependency under `<dependencies>`:

	<copy>
	<dependency>
    	<groupId>io.micronaut.micrometer</groupId>
    	<artifactId>micronaut-micrometer-registry-prometheus</artifactId>
    	<scope>runtime</scope>
	</dependency>
	</copy>		


Before proceeding you should refresh your project dependencies:

![Project Dialog](../images/dependency-refresh.png)		

Then add the following configuration to your `application.yml`:

	<copy>
	micronaut:
	  metrics:
	    enabled: true
	    export:
	      prometheus:
	        enabled: true
	</copy>	

To access application metrics in JSON format you can visit: `http://localhost:8080/metrics`	

To access application metrics in Prometheus format you can visit: `http://localhost:8080/prometheus`

The latter is the URL you can use to configure Prometheus to scrape metrics from the service with.

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar