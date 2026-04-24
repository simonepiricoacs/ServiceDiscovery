package it.water.service.discovery.model;

/**
 * Custom actions for Service Discovery module.
 * Extends the default CRUD actions with the only service-specific runtime
 * operation currently enforced by the registry.
 */
public class ServiceDiscoveryActions {

    /**
     * Action for performing health checks on registered services
     */
    public static final String HEALTH_CHECK = "health_check";

    private ServiceDiscoveryActions() {
        // Utility class
    }
}
