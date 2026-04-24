package it.water.service.discovery.service;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.service.discovery.api.ConfigChangeListener;
import it.water.service.discovery.api.ConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory default implementation of {@link ConfigManager}.
 *
 * <p><b>Reserved extension point — not active in v3.0.0.</b> Registered as the
 * default {@link FrameworkComponent} with {@code priority = 0} so that a
 * higher-priority backend (ZooKeeper, etcd, Consul) can override it via the
 * normal Water component-priority mechanism without code changes.
 *
 * <p>No production code path currently calls this component. It is safe to keep
 * in the classpath: the Water runtime instantiates it once as an inert
 * singleton and no entries are ever added unless an external actor explicitly
 * injects it and invokes the API. It is not part of the heartbeat/liveness
 * flow and does not participate in ServiceDiscovery registration cleanup.
 *
 * <p>The {@link #clear()}, {@link #getServiceCount()} and
 * {@link #getTotalConfigurationCount()} helpers are intended for unit tests and
 * for the day this subsystem is reactivated; they are not part of the
 * {@code ConfigManager} contract.
 */
@FrameworkComponent(priority = 0)
public class InMemoryConfigManager implements ConfigManager {

    private final Map<String, Map<String, String>> configurations = new ConcurrentHashMap<>();
    private final Map<String, List<ConfigChangeListener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void saveConfiguration(String serviceName, String instanceId, String configuration) {
        configurations.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                     .put(instanceId, configuration);
        notifyListeners(serviceName, instanceId, configuration);
    }

    @Override
    public String loadConfiguration(String serviceName, String instanceId) {
        return configurations.getOrDefault(serviceName, Collections.emptyMap())
                            .get(instanceId);
    }

    @Override
    public void deleteConfiguration(String serviceName, String instanceId) {
        Map<String, String> serviceConfigs = configurations.get(serviceName);
        if (serviceConfigs != null) {
            serviceConfigs.remove(instanceId);
            if (serviceConfigs.isEmpty()) {
                configurations.remove(serviceName);
            }
        }
    }

    @Override
    public Map<String, String> loadServiceConfigurations(String serviceName) {
        return new HashMap<>(configurations.getOrDefault(serviceName, Collections.emptyMap()));
    }

    @Override
    public void registerConfigurationChangeListener(String serviceName, ConfigChangeListener listener) {
        listeners.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    @Override
    public boolean configurationExists(String serviceName, String instanceId) {
        return configurations.containsKey(serviceName) &&
               configurations.get(serviceName).containsKey(instanceId);
    }

    /**
     * Notifies all registered listeners when a configuration changes.
     *
     * @param serviceName the name of the service
     * @param instanceId the unique instance identifier
     * @param configuration the new configuration content
     */
    private void notifyListeners(String serviceName, String instanceId, String configuration) {
        List<ConfigChangeListener> serviceListeners = listeners.get(serviceName);
        if (serviceListeners != null) {
            serviceListeners.forEach(listener ->
                listener.onConfigurationChanged(serviceName, instanceId, configuration));
        }
    }

    /**
     * Clears all configurations. Intended for tests and for reactivation scenarios.
     */
    public void clear() {
        configurations.clear();
    }

    /**
     * Returns the number of services with stored configurations.
     *
     * @return the number of services
     */
    public int getServiceCount() {
        return configurations.size();
    }

    /**
     * Returns the total number of stored configurations across all services.
     *
     * @return the total configuration count
     */
    public int getTotalConfigurationCount() {
        return configurations.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
}
