package it.water.service.discovery.service.rest.spring;

import it.water.service.discovery.model.ServiceRegistration;
import it.water.service.discovery.service.rest.ServiceRegistrationInternalRestControllerImpl;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Spring MVC controller for the internal ServiceRegistration endpoints.
 */
@RestController
public class ServiceRegistrationInternalSpringRestControllerImpl extends ServiceRegistrationInternalRestControllerImpl
        implements ServiceRegistrationInternalSpringRestApi {

    @Override
    @SuppressWarnings("java:S1185")
    public ServiceRegistration register(ServiceRegistration serviceRegistration) {
        return super.register(serviceRegistration);
    }

    @Override
    @SuppressWarnings("java:S1185")
    public List<ServiceRegistration> listAvailable() {
        return super.listAvailable();
    }

    @Override
    @SuppressWarnings("java:S1185")
    public void deregister(String serviceName, String instanceId) {
        super.deregister(serviceName, instanceId);
    }

    @Override
    @SuppressWarnings("java:S1185")
    public void heartbeat(String serviceName, String instanceId) {
        super.heartbeat(serviceName, instanceId);
    }
}
