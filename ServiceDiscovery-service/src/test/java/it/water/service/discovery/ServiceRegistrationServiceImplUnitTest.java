package it.water.service.discovery;

import it.water.service.discovery.api.ServiceRegistrationRepository;
import it.water.service.discovery.api.ServiceRegistrationSystemApi;
import it.water.service.discovery.model.ServiceRegistration;
import it.water.service.discovery.model.ServiceStatus;
import it.water.service.discovery.service.ServiceRegistrationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRegistrationServiceImplUnitTest {

    @Test
    void registerDeregisterAndHeartbeatShouldDelegateToSystemService() {
        ServiceRegistrationSystemApi systemApi = mock(ServiceRegistrationSystemApi.class);
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();
        service.setSystemService(systemApi);
        ServiceRegistration registration = registration("catalog", "catalog-1", ServiceStatus.UP);
        when(systemApi.registerInternal(registration)).thenReturn(registration);

        Assertions.assertSame(registration, service.register(registration));
        service.deregister("catalog", "catalog-1");
        service.updateHeartbeat("catalog", "catalog-1");

        verify(systemApi).registerInternal(registration);
        verify(systemApi).deregisterByKey("catalog", "catalog-1");
        verify(systemApi).updateHeartbeatInternal("catalog", "catalog-1");
    }

    @Test
    void repositoryQueriesShouldReturnRepositoryResults() {
        ServiceRegistrationRepository repository = mock(ServiceRegistrationRepository.class);
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();
        service.setRepository(repository);
        ServiceRegistration upRegistration = registration("catalog", "catalog-1", ServiceStatus.UP);
        when(repository.findByServiceName("catalog")).thenReturn(List.of(upRegistration));
        when(repository.findByStatus(ServiceStatus.UP)).thenReturn(List.of(upRegistration));

        Assertions.assertEquals(List.of(upRegistration), service.findByServiceName("catalog"));
        Assertions.assertEquals(List.of(upRegistration), service.findByStatus(ServiceStatus.UP));
        Assertions.assertEquals(List.of(upRegistration), service.getAvailableServices());
    }

    @Test
    void findByTagsShouldReturnEmptyListForNullOrEmptyTags() {
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();

        Assertions.assertTrue(service.findByTags(null).isEmpty());
        Assertions.assertTrue(service.findByTags(Set.of()).isEmpty());
    }

    @Test
    void findByTagsShouldMergeRepositoryResultsWithoutDuplicates() {
        ServiceRegistrationRepository repository = mock(ServiceRegistrationRepository.class);
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();
        service.setRepository(repository);
        ServiceRegistration catalog = registration("catalog", "catalog-1", ServiceStatus.UP);
        ServiceRegistration orders = registration("orders", "orders-1", ServiceStatus.UP);
        when(repository.findByTagsContaining("public")).thenReturn(List.of(catalog, orders));
        when(repository.findByTagsContaining("rest")).thenReturn(List.of(catalog));

        List<ServiceRegistration> results = service.findByTags(Set.of("public", "rest"));

        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.contains(catalog));
        Assertions.assertTrue(results.contains(orders));
    }

    @Test
    void performHealthCheckShouldReturnOutOfServiceWhenRegistrationIsMissing() {
        ServiceRegistrationRepository repository = mock(ServiceRegistrationRepository.class);
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();
        service.setRepository(repository);
        when(repository.findByServiceNameAndInstanceId("catalog", "missing")).thenReturn(null);

        Assertions.assertEquals(ServiceStatus.OUT_OF_SERVICE, service.performHealthCheck("catalog", "missing"));
    }

    @Test
    void performHealthCheckShouldReturnCurrentRegistrationStatus() {
        ServiceRegistrationRepository repository = mock(ServiceRegistrationRepository.class);
        ServiceRegistrationServiceImpl service = new ServiceRegistrationServiceImpl();
        service.setRepository(repository);
        ServiceRegistration registration = registration("catalog", "catalog-1", ServiceStatus.DOWN);
        when(repository.findByServiceNameAndInstanceId("catalog", "catalog-1")).thenReturn(registration);

        Assertions.assertEquals(ServiceStatus.DOWN, service.performHealthCheck("catalog", "catalog-1"));
    }

    private ServiceRegistration registration(String serviceName, String instanceId, ServiceStatus status) {
        return new ServiceRegistration(serviceName, "1.0.0", instanceId,
                "http://localhost:8080", "http", status);
    }
}
