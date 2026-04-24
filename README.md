# ServiceDiscovery Module

The **ServiceDiscovery** module is the Water service registry. Microservices use it to:

- register themselves on startup
- refresh liveness through heartbeat
- deregister on shutdown
- expose the current list of `UP` instances to internal consumers such as `ApiGateway`

## Sub-modules

| Sub-module | Description |
|---|---|
| **ServiceDiscovery-api** | Public API, system API, repository API and REST contracts |
| **ServiceDiscovery-model** | `ServiceRegistration`, `ServiceStatus`, `ServiceDiscoveryActions` |
| **ServiceDiscovery-service** | JPA repository, service/system implementations, JAX-RS controllers, cleanup scheduler |
| **ServiceDiscovery-service-spring** | Spring MVC controllers and Spring Boot wiring |

## ServiceRegistration model

The registry entry is identified by the logical key:

- `serviceName`
- `instanceId`

The most relevant fields are:

| Field | Description |
|---|---|
| `serviceName` | Logical service identifier shared by all instances of the same service |
| `serviceVersion` | Service version reported by the instance |
| `instanceId` | Unique runtime instance identifier |
| `endpoint` | Reachable base endpoint for the instance |
| `protocol` | Transport protocol, typically `http` |
| `status` | Registry status: `STARTING`, `UP`, `DOWN`, `OUT_OF_SERVICE` |
| `lastHeartbeat` | Last liveness update received by the registry |
| `healthCheckInterval` | Optional heartbeat/health-check cadence metadata |
| `healthCheckEndpoint` | Optional endpoint metadata for health probing |
| `metadata` | Reserved extension map persisted and exposed by the REST contract |
| `configuration` | Reserved extension payload persisted and exposed by the REST contract |

`metadata` and `configuration` are intentionally kept in the entity contract, but the current module does not interpret them.

## Security model

`ServiceDiscoveryActions` currently exposes a single service-specific action:

- `health_check`

Default roles:

| Role | Permissions |
|---|---|
| **serviceDiscoveryManager** | `save`, `update`, `find`, `find_all`, `remove`, `health_check` |
| **serviceDiscoveryViewer** | `find`, `find_all` |
| **serviceDiscoveryOperator** | `find`, `find_all`, `update`, `health_check` |

## REST boundaries

### Public authenticated API

Base path: `/water/api/serviceregistration`

This is the standard CRUD boundary. It remains JWT-protected and is intended for administrative access.

| Method | Path |
|---|---|
| `POST` | `/water/api/serviceregistration` |
| `PUT` | `/water/api/serviceregistration` |
| `GET` | `/water/api/serviceregistration/{id}` |
| `GET` | `/water/api/serviceregistration` |
| `DELETE` | `/water/api/serviceregistration/{id}` |

### Internal anonymous API

Base path: `/water/internal/serviceregistration`

This boundary is intentionally anonymous and is used for infrastructure-to-infrastructure traffic inside the runtime.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/water/internal/serviceregistration/register` | Service self-registration |
| `GET` | `/water/internal/serviceregistration/available` | Read only `UP` instances |
| `DELETE` | `/water/internal/serviceregistration/{serviceName}/{instanceId}` | Service deregistration by logical key |
| `PUT` | `/water/internal/serviceregistration/heartbeat/{serviceName}/{instanceId}` | Heartbeat refresh |

## Runtime behavior

The module is centered on the `ServiceRegistrationSystemApi` system layer.

Relevant operations:

- `registerInternal(...)`
- `deregisterByKey(serviceName, instanceId)`
- `updateHeartbeatInternal(serviceName, instanceId)`
- `cleanupInactiveServices(thresholdSeconds)`
- `performBatchHealthCheck()`

Important runtime semantics:

- heartbeat on a missing registration returns `404` through the internal REST boundary
- cleanup marks stale registrations out of the healthy set
- `available` returns only instances currently marked `UP`

## Repository notes

The repository exposes lookup by:

- logical key `serviceName + instanceId`
- `serviceName`
- `status`
- tags
- stale heartbeat timestamps

Bulk status/heartbeat transitions use JPQL update statements to keep those operations atomic.

## Integration with ApiGateway

`ApiGateway` synchronizes its local cache against:

```text
GET /water/internal/serviceregistration/available
```

So the gateway consumes the internal anonymous boundary, not the public authenticated CRUD API.

## Reserved / inactive components

The following types are part of the module's public API surface but are **not wired** to any production code path in v3.0.0. They exist as reserved extension points for a future distributed-configuration layer (e.g., ZooKeeper, etcd, Consul):

- `ConfigManager` (API) — contract for a per-service configuration store
- `ConfigChangeListener` (API) — callback for real-time config updates
- `InMemoryConfigManager` (service) — default implementation registered as `@FrameworkComponent(priority = 0)` so a higher-priority backend can override it

These types are intentionally kept even though no current code path invokes them; see the Javadocs on each type for the full rationale.

## Testing

- JUnit covers repository, service and system-layer behavior
- REST boundaries are covered via Karate
- Internal API behavior is validated separately from the public CRUD API
