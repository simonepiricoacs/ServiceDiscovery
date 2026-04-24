package it.water.service.discovery.api;

import java.util.Map;

/**
 * Contract for a per-service configuration store (save / load / delete / watch).
 *
 * <p><b>Reserved extension point — not active in v3.0.0.</b> The default
 * implementation {@code it.water.service.discovery.service.InMemoryConfigManager}
 * is registered in the Water runtime but no production code path currently invokes
 * this API: the ServiceDiscovery registry persists {@code ServiceRegistration}
 * instances and relies on internal REST endpoints for liveness, while runtime
 * configuration distribution is intentionally deferred.
 *
 * <p>The contract is kept in the public API so alternative backends (ZooKeeper,
 * etcd, Consul) can be plugged in later via `@FrameworkComponent` priority
 * overrides without breaking callers. Removing it would be a breaking API
 * change for clients that may already depend on the type.
 *
 * <p>Do NOT inject or call these methods from new code until the
 * distributed-configuration layer is reactivated. In particular, the current
 * heartbeat/liveness flow does not read or write configuration through this
 * API: liveness is handled by internal ServiceDiscovery REST endpoints and the
 * cleanup scheduler.
 */
public interface ConfigManager {

    /**
     * Saves configuration for a specific service instance.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     * @param configuration the configuration content (JSON/YAML/XML)
     */
    void saveConfiguration(String serviceName, String instanceId, String configuration);

    /**
     * Loads configuration for a specific service instance.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     * @return the configuration content, or null if not found
     */
    String loadConfiguration(String serviceName, String instanceId);

    /**
     * Deletes configuration for a specific service instance.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     */
    void deleteConfiguration(String serviceName, String instanceId);

    /**
     * Loads all configurations for a specific service (all instances).
     *
     * @param serviceName the name of the service
     * @return map of instanceId -> configuration
     */
    Map<String, String> loadServiceConfigurations(String serviceName);

    /**
     * Registers a listener to be notified when configuration changes.
     * Useful for implementations that support real-time updates (e.g., ZooKeeper).
     *
     * @param serviceName the name of the service to watch
     * @param listener the listener to notify on configuration changes
     */
    void registerConfigurationChangeListener(String serviceName, ConfigChangeListener listener);

    /**
     * Checks if configuration exists for a specific service instance.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     * @return true if configuration exists, false otherwise
     */
    boolean configurationExists(String serviceName, String instanceId);
}
