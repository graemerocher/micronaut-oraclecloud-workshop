# Configuring Micronaut

## Introduction
In this lab you will learn how to apply configuration changes to your Micronaut application.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Learn how to modify a Micronaut applications configuration
* Learn how to alter configuration via the environment
* Understand Micronaut environments

### Prerequisites
- Access to your project instance

## Modifying Application Configuration

Micronaut applications are Cloud Native in that they are able to automatically detect their environment, resolving configuration from the environment as needed.

By default Micronaut will search any configuration files you have located in `src/main/resources` and has out of the box support for many popular formats such as `.properties`, `.yml`, `.json` and so on.

Let's try and modfify the application configuration. Alter the `src/main/resources/application.yml` file and add configuration to alter the [server port](https://docs.micronaut.io/latest/guide/configurationreference.html#io.micronaut.http.server.HttpServerConfiguration) to 8081 by default:

```yaml
micronaut:
  application:
    name: demo
  server:
    port: 8081
```

Now run your application again and you will notice it starts on port 8081:

```
[main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [oraclecloud, cloud]
[main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1836ms. Server Running: http://localhost:8081
```

Take special note that Micronaut has detected the `oraclecloud` environment since this application is running in Oracle Cloud.


## Environment Specific Configuration

You can configure Micronaut differently based on the currently detected environment.

To demonstrate this create a new configuration file in `.properties` file format called `src/main/resources/application-oraclecloud.properties` and place the following configuration within the file:

```
micronaut.server.port=8085
```

Now run the application again and note the output:

```
[main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [oraclecloud, cloud]
[main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1952ms. Server Running: http://localhost:8085
```

As you can see the application starts on port 8085 because the `application-oraclecloud.properties` file takes precedence as being more specific for the current environment and overrides the value configured in the previous section.

## Using Environment Variables

Micronaut has a specific property resolution order that which is [described in the user guide ](https://docs.micronaut.io/latest/guide/index.html#propertySource). Configuration is resolved in the following precedence:

1. Command line arguments
2. Java System Properties
3. OS environment variables
4. Configuration files loaded in order from the system property 'micronaut.config.files' or the environment variable `MICRONAUT_CONFIG_FILES`
5. Environment-specific properties from application-{environment}.{extension}
6. Application-specific properties from application.{extension} 

To demonstrate this open Terminal and run the following commands:

```
$ export MICRONAUT_SERVER_PORT=8090
$ ./gradlew run
```

And note the output:

```
[main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [oraclecloud, cloud]
[main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1952ms. Server Running: http://localhost:8090
```

As you can see the configuration resolved via the environment variable is resolved and replaces the values that were defined earlier in either `application.yml` or `application-oraclecloud.properties`.

Note that with environment variables you use upper-case and underscore separated variable names which are normalized into the lower-case equivalent. In this case `MICRONAUT_SERVER_PORT` becomes `micronaut.server.port`.

## Rolling Back Configuration Changes

To continue with the lab let's roll back our configuration changes. To reset the state of the terminal run the following command:

```
$ unset MICRONAUT_SERVER_PORT
```

Then remove the Oracle Cloud specific configuration by simply deleting `application-oraclecloud.properties`.

Finally, restore your `application.yml` to a clean slate by replacing its contents with:

```yaml
micronaut:
	application:
		name: demo
```