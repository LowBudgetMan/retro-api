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

### The Websocket Base URL

The API requires a `websocket.base-url` property to be configured in all running instances. This property specifies the 
base URL for websocket connections and is used by the application to handle real-time notifications. When using the built-in 
STOMP broker (default configuration), set this property to the application's own hostname and port, e.g., `ws://your-hostname:8080` 
or `ws://localhost:8080` for local development.

Example configuration:

```yaml
websocket:
  base-url: ws://localhost:8080
```

### Running the application

The API can be run locally using the gradle `./gradlew bootRun` task. To run with additional profile, pass the profiles 
in as an argument `./gradlew bootRun --args='--spring.profiles.active=local'`.

## Metrics (OTLP export)

The API collects metrics with Micrometer (HTTP request/response latency, per-service-method timings, and any `@Timed` 
timers) and can push them to any OTLP-compatible collector — bring your own (Telegraf/InfluxDB, an OpenTelemetry 
Collector, Grafana Alloy, etc.). **Export is disabled by default**: if you set nothing, no metrics leave the process and 
there is no log noise from failed pushes. No rebuild is required to enable it — this works the same for the JVM and 
GraalVM native images.

To enable export, set these environment variables (e.g. in your compose file or deployment):

```
MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED=true
MANAGEMENT_OTLP_METRICS_EXPORT_URL=http://<your-collector>:4318/v1/metrics
MANAGEMENT_OTLP_METRICS_EXPORT_STEP=60s    # optional, push interval (default 60s)
DEPLOYMENT_ENVIRONMENT=production           # optional, tags all metrics with deployment.environment
```

`DEPLOYMENT_ENVIRONMENT` is attached as the `deployment.environment` OTLP resource attribute (defaults to 
`unknown`), so a single collector/backend can distinguish instances — e.g. dashboards can filter or switch between 
`local`, `production`, etc. Set a distinct value per deployment.

`/actuator/health` is exposed publicly for healthchecks; the metrics HTTP endpoint is not exposed in production (only 
under the `local` profile). See [`docs/metrics.md`](docs/metrics.md) for the full list of collected metrics and details.
