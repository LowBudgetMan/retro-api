# Retro API
This project is the backing API for a retrospective tool

## Running Locally

There are three external services that the API interacts with, each managed with Docker:
* A remote Auth Server capable of handling OAuth2 JWT tokens (this is required)
* A remote SQL database (default is an in-memory H2 database)
* A remote STOMP broker (default is a simple broker managed by Spring)

These services can be managed through the included docker-compose file and are explained more in-depth below. To start 
the services, run `docker compose up -d`.

### The Auth Server
While the API will work with any OAuth2 compatible server that supports JWTs, we have included a KeyStore image in the 
compose file so that all authentication and authorization can be managed locally. This local KeyCloak instance uses a 
preconfigured realm that has Postman as a client and the repository includes a postman collection with the local KeyCloak 
already configured as a token provider. This should make using the API's authenticated endpoints much easier.

To use the settings for the provided KeyCloak instance in the API, just include the `local` profile when running the 
application. To point to a different Auth server, just update the issuer url in `application-local.yml` or create your 
own application properties file with the appropriate config replaced.

### The SQL Database
The API will work with any SQL Server, all migrations are handled at startup using Liquibase, and by default it runs 
against an in-memory H2 database. If an external database is desired, add the `dockerdb` profile when running and the 
application will connect to the PostgreSQL instance managed by docker compose.

### The STOMP Broker
By default, the API uses Spring's simple STOMP broker to notify clients of events. It can be replaced with external brokers
specified using the `broker.relay` properties. For example:
```yaml
broker:
  relay:
    relay-host: localhost
    relay-port: 61613
    relay-username: guest
    relay-password: guest
```
This example config can be found in `application-remotebroker.yml` and points to the RabbitMQ instance managed by docker.
This modified RabbitMQ instance has the `rabbitmq_mqtt`, `rabbitmq_federation_management`, and `rabbitmq_stomp` plugins 
enabled.

### Running the application

The API can be run locally using the gradle `./gradlew bootRun` task. To run with additional profile, pass the profiles 
in as an argument `./gradlew bootRun --args='--spring.profiles.active=local'`.