package it.water.service.discovery;

import it.water.core.api.registry.ComponentRegistry;
import it.water.service.discovery.api.ServiceRegistrationSystemApi;
import it.water.service.discovery.api.options.ServiceDiscoveryCleanupOptions;
import it.water.service.discovery.service.ServiceDiscoveryCleanupSchedulerComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceDiscoveryCleanupSchedulerComponentTest {

    @Test
    void cleanupSchedulerInvokesSystemCleanupPeriodically() {
        ServiceDiscoveryCleanupOptions cleanupOptions = mock(ServiceDiscoveryCleanupOptions.class);
        ServiceRegistrationSystemApi systemApi = mock(ServiceRegistrationSystemApi.class);
        when(cleanupOptions.getCleanupIntervalSeconds()).thenReturn(1L);
        when(cleanupOptions.getCleanupThresholdSeconds()).thenReturn(5L);

        ServiceDiscoveryCleanupSchedulerComponent component = new ServiceDiscoveryCleanupSchedulerComponent();
        component.setCleanupOptions(cleanupOptions);
        component.setServiceRegistrationSystemApi(systemApi);

        try {
            component.onActivate();
            verify(systemApi, timeout(1500).atLeastOnce()).cleanupInactiveServices(5);
        } finally {
            component.onDeactivate();
        }
    }

    @Test
    void cleanupSchedulerRetriesBootstrapUntilDependenciesBecomeAvailable() {
        ServiceDiscoveryCleanupOptions cleanupOptions = mock(ServiceDiscoveryCleanupOptions.class);
        ServiceRegistrationSystemApi systemApi = mock(ServiceRegistrationSystemApi.class);
        ComponentRegistry componentRegistry = mock(ComponentRegistry.class);
        when(cleanupOptions.getCleanupIntervalSeconds()).thenReturn(1L);
        when(cleanupOptions.getCleanupThresholdSeconds()).thenReturn(5L);

        AtomicInteger systemApiLookups = new AtomicInteger();
        when(componentRegistry.findComponent(ServiceRegistrationSystemApi.class, null))
                .thenAnswer(invocation -> systemApiLookups.incrementAndGet() >= 2 ? systemApi : null);
        when(componentRegistry.findComponent(ServiceDiscoveryCleanupOptions.class, null))
                .thenReturn(cleanupOptions);

        ServiceDiscoveryCleanupSchedulerComponent component = new ServiceDiscoveryCleanupSchedulerComponent();
        component.setComponentRegistry(componentRegistry);

        try {
            component.onActivate();
            verify(systemApi, timeout(2500).atLeastOnce()).cleanupInactiveServices(5);
        } finally {
            component.onDeactivate();
        }
    }
}
