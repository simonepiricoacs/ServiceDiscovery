package it.water.service.discovery.api;

/**
 * Callback interface for implementations of {@link ConfigManager} that support
 * real-time configuration updates (for example, backed by ZooKeeper watchers).
 *
 * <p><b>Reserved extension point — not active in v3.0.0.</b> Kept for forward
 * compatibility alongside {@link ConfigManager}: see that type's Javadoc for
 * the full rationale. No production code path currently registers listeners
 * through this interface.
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
