# ServiceDiscovery Module — Microservice Registry

## Purpose

`ServiceDiscovery` is the Water runtime registry for service instances. Its job is narrow:

- accept internal service registrations
- keep liveness updated through heartbeat
- expose the current `UP` instances to internal consumers
- keep the public CRUD boundary separate from the internal infrastructure boundary

## Sub-modules

| Sub-module | Runtime | Key classes |
|---|---|---|
| `ServiceDiscovery-api` | All | `ServiceRegistrationApi`, `ServiceRegistrationSystemApi`, repository and REST contracts |
| `ServiceDiscovery-model` | All | `ServiceRegistration`, `ServiceStatus`, `ServiceDiscoveryActions` |
| `ServiceDiscovery-service` | Water/OSGi | Repository, service/system layer, JAX-RS controllers, cleanup scheduler |
| `ServiceDiscovery-service-spring` | Spring Boot | Spring MVC controllers and application wiring |

## Security model

`ServiceDiscoveryActions` currently defines only one service-specific action:

```java
public class ServiceDiscoveryActions {
    public static final String HEALTH_CHECK = "health_check";
}
```

Default roles:

| Role | Allowed actions |
|---|---|
| `serviceDiscoveryManager` | `save`, `update`, `find`, `find_all`, `remove`, `health_check` |
| `serviceDiscoveryViewer` | `find`, `find_all` |
| `serviceDiscoveryOperator` | `find`, `find_all`, `update`, `health_check` |

## ServiceRegistration model

The logical identity of a registration is:

- `serviceName`
- `instanceId`

Operationally important fields:

- `endpoint`
- `protocol`
- `status`
- `lastHeartbeat`
- `healthCheckInterval`
- `healthCheckEndpoint`

Two fields are intentionally kept as reserved extension points:

- `metadata`
- `configuration`

They are persisted and exposed by the REST contract, but the current registry logic does not interpret them.

## REST boundaries

### Public authenticated API

Base path:

```text
/water/api/serviceregistration
```

This is the JWT-protected CRUD boundary.

### Internal anonymous API

Base path:

```text
/water/internal/serviceregistration
```

This is the infrastructure boundary used by microservices and `ApiGateway`.

Internal endpoints:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/water/internal/serviceregistration/register` | Self-registration |
| `GET` | `/water/internal/serviceregistration/available` | Read only `UP` instances |
| `DELETE` | `/water/internal/serviceregistration/{serviceName}/{instanceId}` | Deregistration by logical key |
| `PUT` | `/water/internal/serviceregistration/heartbeat/{serviceName}/{instanceId}` | Heartbeat refresh |

## System-layer semantics

The authoritative runtime behavior lives in `ServiceRegistrationSystemApi` / `ServiceRegistrationSystemServiceImpl`.

Important rules:

- heartbeat on a missing registration returns `404` from the internal REST boundary
- `available` returns only registrations currently marked `UP`
- cleanup removes stale registrations from the healthy set
- deregistration and heartbeat operate on the logical key `serviceName + instanceId`, not only on DB ids

## Repository notes

`ServiceRegistrationRepositoryImpl` uses QueryBuilder for lookups and JPQL bulk updates for atomic state transitions.

This is intentional:

- QueryBuilder for read-side filters
- JPQL `update` / `delete` for bulk transitions such as heartbeat and status updates

## Integration with ApiGateway

`ApiGateway` syncs against:

```text
GET /water/internal/serviceregistration/available
```

So the gateway uses the internal anonymous boundary, not the public authenticated CRUD API.

## Reserved / inactive components

The following types are part of the module's public API surface but are **not wired** to any production code path in v3.0.0. They are kept as reserved extension points for a future distributed-configuration layer (ZooKeeper, etcd, Consul, …):

- `it.water.service.discovery.api.ConfigManager` — contract for a per-service configuration store (save / load / delete / watch)
- `it.water.service.discovery.api.ConfigChangeListener` — functional callback for real-time config updates
- `it.water.service.discovery.service.InMemoryConfigManager` — default `@FrameworkComponent(priority = 0)` implementation, so a higher-priority backend can override it without code changes

Rules:

- do **not** `@Inject` or call these types from new code until the subsystem is reactivated
- do **not** remove them: they are the public hook point for a pluggable config backend
- when the subsystem is reactivated, remove the "reserved" note from the Javadocs and update this section

## Testing guidance

- repository/service/system behavior: JUnit
- REST boundaries: Karate
- do not add direct controller JUnit tests for the REST layer
