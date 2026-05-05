package it.water.service.discovery;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.service.discovery.model.ServiceDiscoveryConstants;
import it.water.service.discovery.service.ServiceDiscoveryCleanupOptionsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceDiscoveryCleanupOptionsImplTest {

    @Test
    void shouldUseDefaultsWhenApplicationPropertiesAreMissing() {
        ServiceDiscoveryCleanupOptionsImpl options = new ServiceDiscoveryCleanupOptionsImpl();

        Assertions.assertEquals(30L, options.getCleanupIntervalSeconds());
        Assertions.assertEquals(120L, options.getCleanupThresholdSeconds());
    }

    @Test
    void shouldReadConfiguredValuesFromApplicationProperties() {
        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        when(applicationProperties.getPropertyOrDefault(ServiceDiscoveryConstants.PROP_CLEANUP_INTERVAL_SECONDS, 30L))
                .thenReturn(45L);
        when(applicationProperties.getPropertyOrDefault(ServiceDiscoveryConstants.PROP_CLEANUP_THRESHOLD_SECONDS, 120L))
                .thenReturn(600L);
        ServiceDiscoveryCleanupOptionsImpl options = new ServiceDiscoveryCleanupOptionsImpl();
        options.setApplicationProperties(applicationProperties);

        Assertions.assertEquals(45L, options.getCleanupIntervalSeconds());
        Assertions.assertEquals(600L, options.getCleanupThresholdSeconds());
    }

    @Test
    void shouldFallbackToDefaultsWhenConfiguredValuesAreInvalid() {
        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        when(applicationProperties.getPropertyOrDefault(ServiceDiscoveryConstants.PROP_CLEANUP_INTERVAL_SECONDS, 30L))
                .thenReturn(30L);
        when(applicationProperties.getPropertyOrDefault(ServiceDiscoveryConstants.PROP_CLEANUP_THRESHOLD_SECONDS, 120L))
                .thenReturn(120L);
        ServiceDiscoveryCleanupOptionsImpl options = new ServiceDiscoveryCleanupOptionsImpl();
        options.setApplicationProperties(applicationProperties);

        Assertions.assertEquals(30L, options.getCleanupIntervalSeconds());
        Assertions.assertEquals(120L, options.getCleanupThresholdSeconds());
    }
}
