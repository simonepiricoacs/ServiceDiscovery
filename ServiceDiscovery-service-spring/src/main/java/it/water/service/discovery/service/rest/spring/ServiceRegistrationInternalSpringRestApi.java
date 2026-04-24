package it.water.service.discovery.service.rest.spring;

import com.fasterxml.jackson.annotation.JsonView;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.service.discovery.api.rest.ServiceRegistrationInternalRestApi;
import it.water.service.discovery.model.ServiceRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Spring MVC counterpart of the internal ServiceRegistration API.
 */
@RequestMapping("/internal/serviceregistration")
@FrameworkRestApi
public interface ServiceRegistrationInternalSpringRestApi extends ServiceRegistrationInternalRestApi {

    @PostMapping("/register")
    @JsonView(WaterJsonView.Public.class)
    ServiceRegistration register(@RequestBody ServiceRegistration serviceRegistration);

    @GetMapping("/available")
    @JsonView(WaterJsonView.Public.class)
    List<ServiceRegistration> listAvailable();

    @DeleteMapping("/{serviceName}/{instanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deregister(@PathVariable("serviceName") String serviceName, @PathVariable("instanceId") String instanceId);

    @PutMapping("/heartbeat/{serviceName}/{instanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void heartbeat(@PathVariable("serviceName") String serviceName, @PathVariable("instanceId") String instanceId);
}
