# Setting up your Database Schema with Database Migration

## Introduction
In this lab you will learn how your Micronaut application can manage the evolution of your database schema with Flyway.

Estimated Lab Time: 10 minutes

### Objectives

In this lab you will:
* Learn how to setup a JDBC connection
* Learn how to configure Flyway
* Understand the benefits of database migration
* Write a SQL schema that will initialize autonomous database

### Prerequisites

- Access to your project instance
- Basic knowledge of SQL

## Configuring Access to Oracle Autonomous Database

In the examples so far we have used in-memory data structures to store data. Let's now configure access to [Oracle Autonomous Database](https://www.oracle.com/autonomous-database/).

First add the following dependencies if you are using Gradle to the `build.gradle` file in the root of your project inside the `dependencies` block:

	<copy>
	runtimeOnly("io.micronaut.sql:micronaut-jdbc-hikari:3.3.4")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")
    runtimeOnly("com.oracle.database.security:oraclepki:19.8.0.0")
    runtimeOnly("com.oracle.database.security:osdt_cert:19.8.0.0")
    runtimeOnly("com.oracle.database.security:osdt_core:19.8.0.0")
	</copy>

Alternatively if you are using Maven add the following dependencies to your `pom.xml` inside the `<dependencies>` element:

	<copy>
    <dependency>
      <groupId>io.micronaut.sql</groupId>
      <artifactId>micronaut-jdbc-hikari</artifactId>
      <scope>compile</scope>
      <version>3.3.4</version>
    </dependency>
    <dependency>
      <groupId>io.micronaut.sql</groupId>
      <artifactId>micronaut-jdbc</artifactId>
      <scope>compile</scope>
      <version>3.3.4</version>
    </dependency>   	
	<dependency>
		<groupId>com.oracle.database.jdbc</groupId>
		<artifactId>ojdbc8</artifactId>
		<scope>runtime</scope>
	</dependency>
	<dependency>
		<groupId>com.oracle.database.security</groupId>
		<artifactId>oraclepki</artifactId>
		<version>19.8.0.0</version>
		<scope>runtime</scope>
	</dependency>		
	<dependency>
		<groupId>com.oracle.database.security</groupId>
		<artifactId>osdt_cert</artifactId>
		<version>19.8.0.0</version>
		<scope>runtime</scope>
	</dependency>			
	<dependency>
		<groupId>com.oracle.database.security</groupId>
		<artifactId>osdt_core</artifactId>
		<version>19.8.0.0</version>
		<scope>runtime</scope>
	</dependency>			
	</copy>

The above configuration adds the Oracle JDBC driver and support for JDBC in Micronaut via the Hikari Connection Pool.

In order to connect to Autonomous Database you typically need to download a [Wallet definition](https://docs.oracle.com/en/cloud/paas/atp-cloud/atpug/wallet-rotate.html#GUID-F0995A6A-78BD-4C9D-9A34-B970BD152CAD) in order to establish a secure connetion.

In this virtual lab the wallet file is already downloaded and extracted to the `/tmp/wallet` directory and the necessary `TNS_ADMIN` environment variable set, so all that is required is to correctly configure the JDBC connection which you can do by modifying the `src/main/resources/application.yml` configuration file with the following settings:

	<copy>
	micronaut:
	  application:
	    name: demo
      executors:
      	io:
      	   type: fixed
      	   n-threads: 75
	  server:
	    thread-selection: IO	
	datasources:
	  default:
	    url: "jdbc:oracle:thin:@${DB_NAME}"
	    driverClassName: oracle.jdbc.OracleDriver
	    databaseName: "${DB_SCHEMA}"
	    username: "${DB_USER}"
	    password: "${DB_PASSWORD}"
	    dialect: ORACLE
	    data-source-properties:
	      oracle:
	        jdbc:
	          fanEnabled: false
	</copy>	

> In addition to the database configuration described below, this configuration also sets `micronaut.server.thread-selection` to `IO` which tells Micronaut to run all server operations on the I/O thread pool since the application is going to be doing primarily blocking operations via JDBC and the underlying server (Netty) is based on an event-loop model. The I/O executor is also configured to occupy 75 threads. This may be adjusted according to your production server.

Within the virtual environment of the lab the following environment variables exist:

* `DB_NAME` - the database name
* `DB_SCHEMA` - the database schema
* `DB_USERNAME` - the database username
* `DB_PASSWORD` - the database password

In the `datasources` configuration we use the ability to specify `${..}` expressions to reference these environment variables within the configuration and appropriately configure the [datasource properties](https://micronaut-projects.github.io/micronaut-sql/latest/guide/configurationreference.html#io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration).

The above configuration configures the "default" datasource to connect to Autonomous database and exposes a `javax.sql.DataSource` bean that can be injected as a bean into your application's classes.

## Configuring Flyway

Once you have configured the `DataSource` the next thing to do is to add a dependency on `micronaut-flyway` to your `build.gradle` configuration inside the `dependencies` block:

	<copy>
    runtimeOnly("io.micronaut.flyway:micronaut-flyway")
	</copy>


Or alternatively if Maven is being used to your `pom.xml` under `<dependencies>`:

	<copy>
	<dependency>
		<groupId>io.micronaut.flyway</groupId>
		<artifactId>micronaut-flyway</artifactId>
		<scope>runtime</scope>
	</dependency>
	</copy>

This enables support for the Open Source [Flyway database migration toolkit](https://flywaydb.org) which allows you to define SQL scripts that manage and version your database schema so that you can gradually evolve the schema along with new versions of your application.

To enable Flyway to run on startup add the following configuration to your `application.yml`:

	<copy>
	flyway:
	  datasources: 
	    default: 
	      enabled: true 
	</copy>

In addition create a new file called `src/main/resources/application-test.yml` which will contain your test configuration and set Flyway to clean the schema when then application starts to ensure tests run with fresh data:

	<copy>
	flyway:
	  datasources:
	    default:
	      clean-schema: true
	</copy>

> Note that in a real world scenario you would setup a separate database to run your tests against

## Defining a SQL Migration Script

The next step is to define a SQL migration script that will create the application's initial schema. To do that create a new SQL script in a file called `src/main/resources/db/migration/V1__create-schema.sql` and add the following SQL:

	<copy>
	CREATE TABLE owner (id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(255) NOT NULL, age NUMBER(2) NOT NULL);
	CREATE TABLE pet (id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(255) NOT NULL, age NUMBER(2) NOT NULL, owner_id NUMBER NOT NULL);
	</copy>

The SQL above will create 2 tables called `owner` and `pet` which will store the data for owners and their pets in Autonomous Database.

Before running your application make sure you refresh your Gradle or Maven dependencies:

![Project Dialog](../images/dependency-refresh.png)

Try run your application now and it should start successfully and run the migration scripts.

You may now *proceed to the next lab*.

### Acknowledgements
- **Owners** - Graeme Rocher, Architect, Oracle Labs - Databases and Optimization
- **Contributors** - Palo Gressa, Todd Sharp, Eric Sedlar