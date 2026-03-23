# ServiceDiscovery Module — Microservice Registry

## Purpose
Provides a **service registry** for Water Framework microservices. Services register themselves on startup (with metadata, health check config, and status), send heartbeats to remain active, and deregister on shutdown. Consumers (e.g., `ApiGateway`) query the registry to discover live instances and their endpoints.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `ServiceDiscovery-api` | All | `ServiceRegistrationApi`, `ServiceRegistrationSystemApi`, `ServiceRegistrationRestApi`, `ServiceRegistrationRepository`, `ConfigManager`, `ConfigChangeListener` |
| `ServiceDiscovery-model` | All | `ServiceRegistration`, `ServiceStatus` enum, `ServiceDiscoveryActions` |
| `ServiceDiscovery-service` | Water/OSGi | Service impl, repository, REST controller, `InMemoryConfigManager` |
| `ServiceDiscovery-service-spring` | Spring Boot | Spring MVC REST controllers, Spring Boot app config |

## ServiceRegistration Entity

```java
@Entity
@Table(name = "service_registration",
       uniqueConstraints = @UniqueConstraint(columnNames = {"serviceName", "instanceId"}))
@AccessControl(
    availableActions = {CrudActions.class, ServiceDiscoveryActions.class},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "serviceDiscoveryManager",
                           actions = {CrudActions.class, ServiceDiscoveryActions.class}),
        @DefaultRoleAccess(roleName = "serviceDiscoveryViewer",
                           actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
        @DefaultRoleAccess(roleName = "serviceDiscoveryOperator",
                           actions = {ServiceDiscoveryActions.HEARTBEAT, CrudActions.FIND, CrudActions.FIND_ALL})
    }
)
public class ServiceRegistration extends AbstractJpaEntity
    implements ProtectedEntity, OwnedResource {

    @NotNull @NoMalitiusCode
    private String serviceName;           // logical service name (e.g., "user-service")

    @NotNull @Column(unique = false)
    private String instanceId;            // unique per instance (e.g., "user-service-1")

    @NotNull @NoMalitiusCode
    private String endpoint;              // base URL (e.g., "http://host:8080")

    @NoMalitiusCode
    private String protocol;              // "http" or "https"

    @NoMalitiusCode
    private String description;

    @NoMalitiusCode
    private String tags;                  // comma-separated tags for filtering

    @Lob @NoMalitiusCode
    private String metadata;              // JSON blob for extra metadata

    private ServiceStatus status;         // enum: STARTING, UP, DOWN, OUT_OF_SERVICE

    private Date lastHeartbeat;
    private int healthCheckInterval;      // seconds
    private String healthCheckEndpoint;   // e.g., "/actuator/health"

    @Lob
    private String configuration;         // JSON: service-specific runtime config

    private long ownerUserId;
}
```

## ServiceStatus Lifecycle

```
STARTING ──► UP ──► DOWN (health check fails)
              │       │
              │       └─► UP (health check recovers)
              │
              └─► OUT_OF_SERVICE (manual shutdown)
```

## ServiceDiscoveryActions (Custom)
```java
public class ServiceDiscoveryActions {
    public static final String HEARTBEAT  = "HEARTBEAT";   // bitmask: 32
    public static final String PROMOTE    = "PROMOTE";     // bitmask: 64 (UP/DOWN transition)
    public static final String CONFIGURE  = "CONFIGURE";   // bitmask: 128 (update configuration)
}
```

## Key Interfaces

### ServiceRegistrationApi / SystemApi
```java
ServiceRegistration save(ServiceRegistration reg);       // register service
ServiceRegistration update(ServiceRegistration reg);     // update registration
ServiceRegistration find(long id);
ServiceRegistration findByNameAndInstance(String serviceName, String instanceId);
List<ServiceRegistration> findByServiceName(String serviceName); // all instances
List<ServiceRegistration> findByStatus(ServiceStatus status);    // active services
PaginatedResult<ServiceRegistration> findAll(int delta, int page, Query filter);
void remove(long id);                                    // deregister

// Heartbeat
void heartbeat(String serviceName, String instanceId);  // update lastHeartbeat + set UP

// Health management
void cleanup(int staleThresholdSeconds);                // set DOWN if no heartbeat
```

### ConfigManager
Manages runtime configuration distributed to registered services:

```java
public interface ConfigManager {
    void setConfig(String serviceName, String key, String value);
    String getConfig(String serviceName, String key);
    Map<String, String> getAllConfig(String serviceName);
    void addChangeListener(String serviceName, ConfigChangeListener listener);
}
```

`InMemoryConfigManager` is the default implementation — suitable for single-node deployments. Replace with ZooKeeper-backed implementation for distributed config.

## Service Registration Pattern (Client Side)

```java
// In your microservice's @OnActivate or @PostConstruct
ServiceRegistration reg = new ServiceRegistration();
reg.setServiceName("my-service");
reg.setInstanceId("my-service-" + UUID.randomUUID());
reg.setEndpoint("http://" + hostname + ":" + port);
reg.setProtocol("http");
reg.setStatus(ServiceStatus.STARTING);
reg.setHealthCheckEndpoint("/actuator/health");
reg.setHealthCheckInterval(30);

ServiceRegistration saved = serviceRegistrationApi.save(reg);

// Start heartbeat (every 25 seconds)
scheduler.scheduleAtFixedRate(() ->
    serviceRegistrationApi.heartbeat("my-service", reg.getInstanceId()),
    0, 25, TimeUnit.SECONDS);

// On shutdown (@OnDeactivate / @PreDestroy)
serviceRegistrationApi.remove(saved.getId());
```

## REST Endpoints

| Method | Path | Permission |
|---|---|---|
| `POST` | `/water/serviceregistrations` | serviceDiscoveryManager |
| `PUT` | `/water/serviceregistrations` | serviceDiscoveryManager |
| `GET` | `/water/serviceregistrations/{id}` | serviceDiscoveryViewer |
| `GET` | `/water/serviceregistrations` | serviceDiscoveryViewer |
| `DELETE` | `/water/serviceregistrations/{id}` | serviceDiscoveryManager |
| `PUT` | `/water/serviceregistrations/heartbeat/{name}/{instance}` | serviceDiscoveryOperator |
| `GET` | `/water/serviceregistrations/byName/{serviceName}` | serviceDiscoveryViewer |

## Default Roles

| Role | Allowed Actions |
|---|---|
| `serviceDiscoveryManager` | Full CRUD + heartbeat + promote + configure |
| `serviceDiscoveryViewer` | FIND, FIND_ALL |
| `serviceDiscoveryOperator` | HEARTBEAT, FIND, FIND_ALL |

## Dependencies
- `it.water.repository.jpa:JpaRepository-api` — `AbstractJpaEntity`
- `it.water.core:Core-permission` — `@AccessControl`, `CrudActions`
- `it.water.rest:Rest-persistence` — `BaseEntityRestApi`

## Integration with ApiGateway
The `ApiGateway` module's `ServiceDiscoveryClientImpl` queries:
```
GET /water/serviceregistrations/byName/{serviceName}
```
and load-balances across returned `UP` instances.

## Testing
- Unit tests: `WaterTestExtension` — test registration lifecycle, heartbeat, cleanup
- REST tests: **Karate only** — never JUnit direct calls to `ServiceRegistrationRestController`

## Code Generation Rules
- `ServiceRegistrationRestController` tested **exclusively via Karate**
- Use `ServiceStatus.UP` check when resolving service instances — never forward to `DOWN` or `OUT_OF_SERVICE` instances
- `configuration` field is a JSON blob — use `ObjectMapper` to serialize/deserialize, not raw string manipulation
- `cleanup()` is a scheduled system operation — call via `SystemApi`, not `Api` (no permission context available in scheduler threads)
