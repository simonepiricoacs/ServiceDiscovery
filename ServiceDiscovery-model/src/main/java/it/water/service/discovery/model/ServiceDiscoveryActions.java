package it.water.service.discovery.model;

/**
 * Custom actions for Service Discovery module.
 * Extends the default CRUD actions with service-specific operations.
 */
public class ServiceDiscoveryActions {

    /**
     * Action for performing health checks on registered services
     */
    public static final String HEALTH_CHECK = "health_check";

    /**
     * Action for registering a new service
     */
    public static final String REGISTER = "register";

    /**
     * Action for deregistering a service
     */
    public static final String DEREGISTER = "deregister";

    /**
     * Action for updating service heartbeat
     */
    public static final String UPDATE_HEARTBEAT = "update_heartbeat";

    private ServiceDiscoveryActions() {
        // Utility class
    }
}