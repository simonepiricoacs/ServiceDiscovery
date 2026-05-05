package it.water.service.discovery;

import it.water.service.discovery.service.InMemoryConfigManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class InMemoryConfigManagerTest {

    @Test
    void saveLoadAndDeleteConfigurationShouldWork() {
        InMemoryConfigManager manager = new InMemoryConfigManager();

        manager.saveConfiguration("catalog", "catalog-1", "{\"enabled\":true}");

        Assertions.assertTrue(manager.configurationExists("catalog", "catalog-1"));
        Assertions.assertEquals("{\"enabled\":true}", manager.loadConfiguration("catalog", "catalog-1"));
        Assertions.assertEquals(1, manager.getServiceCount());
        Assertions.assertEquals(1, manager.getTotalConfigurationCount());

        manager.deleteConfiguration("catalog", "catalog-1");

        Assertions.assertFalse(manager.configurationExists("catalog", "catalog-1"));
        Assertions.assertNull(manager.loadConfiguration("catalog", "catalog-1"));
        Assertions.assertEquals(0, manager.getServiceCount());
    }

    @Test
    void loadServiceConfigurationsShouldReturnDefensiveCopy() {
        InMemoryConfigManager manager = new InMemoryConfigManager();
        manager.saveConfiguration("catalog", "catalog-1", "one");
        manager.saveConfiguration("catalog", "catalog-2", "two");

        Map<String, String> configurations = manager.loadServiceConfigurations("catalog");
        configurations.clear();

        Assertions.assertEquals(2, manager.getTotalConfigurationCount());
        Assertions.assertEquals("one", manager.loadConfiguration("catalog", "catalog-1"));
    }

    @Test
    void listenerShouldBeNotifiedOnlyForMatchingService() {
        InMemoryConfigManager manager = new InMemoryConfigManager();
        AtomicReference<String> notification = new AtomicReference<>();

        manager.registerConfigurationChangeListener("catalog",
                (serviceName, instanceId, newConfiguration) ->
                        notification.set(serviceName + ":" + instanceId + ":" + newConfiguration));

        manager.saveConfiguration("orders", "orders-1", "ignored");
        Assertions.assertNull(notification.get());

        manager.saveConfiguration("catalog", "catalog-1", "updated");

        Assertions.assertEquals("catalog:catalog-1:updated", notification.get());
    }

    @Test
    void clearShouldRemoveAllConfigurations() {
        InMemoryConfigManager manager = new InMemoryConfigManager();
        manager.saveConfiguration("catalog", "catalog-1", "one");
        manager.saveConfiguration("orders", "orders-1", "two");

        manager.clear();

        Assertions.assertEquals(0, manager.getServiceCount());
        Assertions.assertEquals(0, manager.getTotalConfigurationCount());
        Assertions.assertTrue(manager.loadServiceConfigurations("catalog").isEmpty());
    }
}
