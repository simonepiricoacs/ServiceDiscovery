package it.water.service.discovery.api;

/**
 * Functional interface for listening to configuration changes.
 * Implementations can react to configuration updates pushed by
 * ConfigManager implementations that support watching (e.g., Zookeeper).
 */
@FunctionalInterface
public interface ConfigChangeListener {

    /**
     * Called when a service configuration changes.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     * @param newConfiguration the new configuration content
     */
    void onConfigurationChanged(String serviceName, String instanceId, String newConfiguration);
}