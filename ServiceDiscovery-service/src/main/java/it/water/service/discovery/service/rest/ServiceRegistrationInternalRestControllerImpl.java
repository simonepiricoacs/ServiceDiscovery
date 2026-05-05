package it.water.service.discovery.service.rest;

import it.water.core.api.service.rest.FrameworkRestController;
import it.water.core.interceptors.annotations.Inject;
import it.water.service.discovery.api.ServiceRegistrationSystemApi;
import it.water.service.discovery.api.rest.ServiceRegistrationInternalRestApi;
import it.water.service.discovery.model.ServiceRegistration;
import it.water.service.discovery.model.ServiceStatus;
import lombok.Setter;
import it.water.repository.entity.model.exceptions.EntityNotFound;
import java.util.List;

/**
 * Thin internal controller that delegates to the system service layer.
 */
@FrameworkRestController(referredRestApi = ServiceRegistrationInternalRestApi.class)
public class ServiceRegistrationInternalRestControllerImpl implements ServiceRegistrationInternalRestApi {

    @Inject
    @Setter
    private ServiceRegistrationSystemApi serviceRegistrationSystemApi;

    @Override
    public ServiceRegistration register(ServiceRegistration serviceRegistration) {
        return serviceRegistrationSystemApi.registerInternal(serviceRegistration);
    }

    @Override
    public List<ServiceRegistration> listAvailable() {
        return serviceRegistrationSystemApi.findByStatus(ServiceStatus.UP);
    }

    @Override
    public void deregister(String serviceName, String instanceId) {
        serviceRegistrationSystemApi.deregisterByKey(serviceName, instanceId);
    }

    @Override
    public void heartbeat(String serviceName, String instanceId) {
        if (!heartbeatInternal(serviceName, instanceId)) {
            throw new EntityNotFound();
        }
    }

    protected boolean heartbeatInternal(String serviceName, String instanceId) {
        return serviceRegistrationSystemApi.updateHeartbeatInternal(serviceName, instanceId);
    }
}
