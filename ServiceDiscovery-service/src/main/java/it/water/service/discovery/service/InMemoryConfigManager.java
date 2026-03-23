package it.water.service.discovery.service;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.service.discovery.api.ConfigChangeListener;
import it.water.service.discovery.api.ConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of ConfigManager.
 * This is the default implementation with priority 0.
 *
 * Stores service configurations in memory using ConcurrentHashMap.
 * Configurations are lost on application restart.
 *
 * For production use with persistent storage, implement a different
 * ConfigManager (e.g., ZookeeperConfigManager) with higher priority.
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
     * Clears all configurations. Useful for testing.
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