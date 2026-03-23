package it.water.service.discovery.api;

import java.util.Map;

/**
 * Interface for managing service configurations.
 * Allows multiple implementations: in-memory, Zookeeper, etcd, Consul, etc.
 *
 * Implementations should be registered as Water Framework components
 * with appropriate priority levels to control injection preference.
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
     * Useful for implementations that support real-time updates (e.g., Zookeeper).
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