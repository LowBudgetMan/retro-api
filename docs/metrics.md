# Metrics (OTLP export)

`retro-api` collects metrics with Micrometer and can push them to any
OTLP-compatible collector. **Export is disabled by default** — if you do
nothing, no metrics leave the process and there is no log noise from failed
push attempts. Bring your own collector and turn it on with environment
variables; no rebuild required.

## What is collected

These meters are always recorded in-memory (whether or not export is on):

- `http.server.requests` — timer per endpoint/method/status for full HTTP
  request/response latency. Percentile-histogram buckets are enabled, so a
  collector can compute latency distributions.
- `retro.service` — timer for every `@Service` method under
  `io.nickreuter.retroapi`, tagged `class` and `method`. Methods annotated
  with `@Timed` are excluded here (they are recorded under their own name
  instead, so they are never double-counted).
- `@Timed("name")` timers — add `@Timed("your.metric.name")` to any
  Spring-managed bean method to record a named timer (optionally with extra
  tags). Example in the codebase: `TeamService.createTeam` →
  `retro.team.create`.
- All meters carry a common tag `application=retro-api`.

## Enabling export

Set these environment variables (e.g. in your compose file or deployment):

```
MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED=true
MANAGEMENT_OTLP_METRICS_EXPORT_URL=http://<your-collector>:4318/v1/metrics
```

Optional:

```
MANAGEMENT_OTLP_METRICS_EXPORT_STEP=60s    # push interval (default 60s)
```

For an InfluxDB homelab, point the URL at a Telegraf container running the
OpenTelemetry input plugin (default OTLP/HTTP port `4318`); Telegraf forwards
to InfluxDB. Any other OTLP/HTTP metrics endpoint (an OpenTelemetry Collector,
Grafana Alloy, etc.) works the same way.

## Actuator endpoints

- `/actuator/health` is exposed and publicly accessible (for container
  healthchecks).
- The `metrics` HTTP endpoint is **not** exposed in production (the OTLP push
  is the collection path). It is exposed only under the `local` Spring profile
  for debugging: `/actuator/metrics` and `/actuator/metrics/{name}`.

## Native image

Metrics work in the GraalVM native image with the same runtime opt-in as the
JVM image: a single native binary serves both "metrics off" (default, idle, no
push, no log noise) and "metrics on" deployments, controlled by the environment
variables above at startup. Verified end-to-end: with export off the native
binary makes zero pushes; with `MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED=true` it
pushes OTLP protobuf payloads to the collector.

Implementation note for maintainers: Spring AOT evaluates `@Conditional`
auto-configuration at build time, so disabling export by default would
otherwise prune the OTLP registry from the native image and break runtime
opt-in. `build.gradle` forces the registry's condition true during AOT
(`processAot` system property) so the registry is baked into the image; actual
publishing remains gated at runtime by `management.otlp.metrics.export.enabled`
(default false). See the comment in `build.gradle`'s `buildNative` block.
