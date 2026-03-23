package it.water.service.discovery.model;

/**
 * Enum representing the health status of a registered service.
 */
public enum ServiceStatus {
    /**
     * Service is starting up
     */
    STARTING,

    /**
     * Service is running and healthy
     */
    UP,

    /**
     * Service is down or unreachable
     */
    DOWN,

    /**
     * Service has been marked as out of service (e.g., due to inactivity)
     */
    OUT_OF_SERVICE,

    /**
     * Service status is unknown
     */
    UNKNOWN
}