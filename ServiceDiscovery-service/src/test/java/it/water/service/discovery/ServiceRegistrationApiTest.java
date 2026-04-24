package it.water.service.discovery;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.model.Role;
import it.water.core.api.permission.PermissionManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.repository.entity.model.exceptions.NoResultException;
import it.water.service.discovery.api.*;
import it.water.service.discovery.model.ServiceRegistration;
import it.water.service.discovery.model.ServiceStatus;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generated with Water Generator.
 * Test class for ServiceRegistration Services.
 * <p>
 * Please use ServiceRegistrationRestTestApi for ensuring format of the json response
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceRegistrationApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private ServiceRegistrationApi serviceRegistrationApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private ServiceRegistrationRepository serviceRegistrationRepository;

    @Inject
    @Setter
    //default permission manager in test environment;
    private PermissionManager permissionManager;

    @Inject
    @Setter
    //test role manager
    private UserManager userManager;

    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    //admin user
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User serviceregistrationManagerUser;
    private it.water.core.api.model.User serviceregistrationViewerUser;
    private it.water.core.api.model.User serviceregistrationOperatorUser;

    private Role serviceregistrationManagerRole;
    private Role serviceregistrationViewerRole;
    private Role serviceregistrationOperatorRole;

    @BeforeAll
    void beforeAll() {
        //cleanup any entities left by other test classes (e.g. Karate)
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        PaginableResult<ServiceRegistration> existing = this.serviceRegistrationApi.findAll(null, -1, -1, null);
        existing.getResults().forEach(entity -> this.serviceRegistrationApi.remove(entity.getId()));
        //getting user
        serviceregistrationManagerRole = roleManager.getRole(ServiceRegistration.DEFAULT_MANAGER_ROLE);
        serviceregistrationViewerRole = roleManager.getRole(ServiceRegistration.DEFAULT_VIEWER_ROLE);
        serviceregistrationOperatorRole = roleManager.getRole(ServiceRegistration.DEFAULT_OPERATOR_ROLE);
        Assertions.assertNotNull(serviceregistrationManagerRole);
        Assertions.assertNotNull(serviceregistrationViewerRole);
        Assertions.assertNotNull(serviceregistrationOperatorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        serviceregistrationManagerUser = userManager.addUser("manager", "name", "lastname", "manager@a.com", "TempPassword1_", "salt", false);
        serviceregistrationViewerUser = userManager.addUser("viewer", "name", "lastname", "viewer@a.com", "TempPassword1_", "salt", false);
        serviceregistrationOperatorUser = userManager.addUser("operator", "name", "lastname", "operator@a.com", "TempPassword1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(serviceregistrationManagerUser.getId(), serviceregistrationManagerRole);
        roleManager.addRole(serviceregistrationViewerUser.getId(), serviceregistrationViewerRole);
        roleManager.addRole(serviceregistrationOperatorUser.getId(), serviceregistrationOperatorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    /**
     * Testing basic injection of basic component for serviceregistration entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        Assertions.assertNotNull(this.serviceRegistrationApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(ServiceRegistrationSystemApi.class, null));
        Assertions.assertNotNull(this.serviceRegistrationRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        ServiceRegistration entity = createServiceRegistration(0);
        entity = this.serviceRegistrationApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("service-0", entity.getServiceName());
        Assertions.assertEquals("instance-0", entity.getInstanceId());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.serviceRegistrationRepository.getQueryBuilderInstance().createQueryFilter("serviceName=service-0");
        ServiceRegistration entity = this.serviceRegistrationApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setEndpoint("http://updated-endpoint:8080");
        entity = this.serviceRegistrationApi.update(entity);
        Assertions.assertEquals("http://updated-endpoint:8080", entity.getEndpoint());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.serviceRegistrationRepository.getQueryBuilderInstance().createQueryFilter("serviceName=service-0");
        ServiceRegistration errorEntity = this.serviceRegistrationApi.find(q);
        Assertions.assertEquals("http://updated-endpoint:8080", errorEntity.getEndpoint());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.serviceRegistrationApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<ServiceRegistration> all = this.serviceRegistrationApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1, all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            ServiceRegistration u = createServiceRegistration(i);
            this.serviceRegistrationApi.save(u);
        }
        PaginableResult<ServiceRegistration> paginated = this.serviceRegistrationApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.serviceRegistrationApi.findAll(null, 7, 2, null);
        Assertions.assertEquals(3, paginated.getResults().size());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<ServiceRegistration> paginated = this.serviceRegistrationApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.serviceRegistrationApi.remove(entity.getId());
        });
        Assertions.assertEquals(0, this.serviceRegistrationApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        ServiceRegistration entity = createServiceRegistration(1);
        this.serviceRegistrationApi.save(entity);
        ServiceRegistration duplicated = this.createServiceRegistration(1);
        //cannot insert new entity which breaks unique constraint on (serviceName, instanceId)
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.serviceRegistrationApi.save(duplicated));
        ServiceRegistration secondEntity = createServiceRegistration(2);
        this.serviceRegistrationApi.save(secondEntity);
        entity.setServiceName("service-2");
        entity.setInstanceId("instance-2");
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.serviceRegistrationApi.update(entity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        ServiceRegistration newEntity = new ServiceRegistration(
                "<script>function(){alert('ciao')!}</script>",
                "1.0.0",
                "test-instance",
                "http://localhost:8080",
                "http",
                ServiceStatus.UP
        );
        Assertions.assertThrows(ValidationException.class, () -> this.serviceRegistrationApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationManagerUser, runtime);
        final ServiceRegistration entity = createServiceRegistration(101);
        ServiceRegistration savedEntity = Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.save(entity));
        savedEntity.setEndpoint("http://new-endpoint:9090");
        Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.update(savedEntity));
        Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.find(savedEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.remove(savedEntity.getId()));

    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationViewerUser, runtime);

        // Viewer cannot save new entities
        final ServiceRegistration entity = createServiceRegistration(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.serviceRegistrationApi.save(entity));

        // Create entity using SystemApi (simulating existing service in the system)
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration systemEntity = serviceRegistrationSystemApi.registerInternal(createServiceRegistration(202));

        // Switch back to viewer
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationViewerUser, runtime);

        // Viewer can search using repository (which doesn't apply permission filters on findAll)
        PaginableResult<ServiceRegistration> all = serviceRegistrationRepository.findAll(-1, 1, null, null);
        Assertions.assertTrue(all.getResults().size() > 0, "Should find at least one entity");
        ServiceRegistration found = all.getResults().iterator().next();

        // Viewer cannot update or remove (permission checks apply)
        found.setEndpoint("http://changed:8080");
        long foundId = found.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.serviceRegistrationApi.update(found));
        Assertions.assertThrows(NoResultException.class, () -> this.serviceRegistrationApi.remove(foundId));
    }

    @Order(12)
    @Test
    void operatorCanUpdateButCannotSaveOrRemove() {
        // Operator cannot save new entities
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationOperatorUser, runtime);
        final ServiceRegistration entity = createServiceRegistration(301);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.serviceRegistrationApi.save(entity));

        // Use SystemApi to create an entity bypassing permissions (simulating an existing service)
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration systemEntity = serviceRegistrationSystemApi.registerInternal(createServiceRegistration(302));

        // Switch to operator - operator can update but not remove
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationOperatorUser, runtime);

        // Find from repository (not API) to get the entity without ownership checks
        ServiceRegistration foundEntity = serviceRegistrationRepository.find(systemEntity.getId());
        Assertions.assertNotNull(foundEntity);

        // Operator can update
        foundEntity.setEndpoint("http://operator-updated:8080");
        Assertions.assertDoesNotThrow(() -> serviceRegistrationRepository.update(foundEntity));

        // Operator cannot remove
        long foundId = foundEntity.getId();
        Assertions.assertThrows(NoResultException.class, () -> this.serviceRegistrationApi.remove(foundId));
    }

    @Order(13)
    @Test
    void ownedResourceShouldBeAccessedOnlyByOwner() {
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationManagerUser, runtime);
        final ServiceRegistration entity = createServiceRegistration(401);
        //saving as manager
        ServiceRegistration savedEntity = Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.serviceRegistrationApi.find(savedEntity.getId()));
        TestRuntimeInitializer.getInstance().impersonate(serviceregistrationViewerUser, runtime);
        //find an owned entity with different user from the creator should raise a NoResultException
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(NoResultException.class, () -> this.serviceRegistrationApi.find(savedEntityId));
    }

    // ========== NEW TESTS: Repository Custom Queries ==========

    @Order(14)
    @Test
    void repositoryFindByServiceNameShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration service1 = createServiceRegistration(501);
        service1.setServiceName("payment-service");
        ServiceRegistration service2 = createServiceRegistration(502);
        service2.setServiceName("payment-service");
        serviceRegistrationApi.save(service1);
        serviceRegistrationApi.save(service2);

        List<ServiceRegistration> found = serviceRegistrationRepository.findByServiceName("payment-service");
        Assertions.assertEquals(2, found.size());
    }

    @Order(15)
    @Test
    void repositoryFindByServiceNameAndInstanceIdShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration found = serviceRegistrationRepository.findByServiceNameAndInstanceId("payment-service", "instance-501");
        Assertions.assertNotNull(found);
        Assertions.assertEquals("payment-service", found.getServiceName());
        Assertions.assertEquals("instance-501", found.getInstanceId());
    }

    @Order(16)
    @Test
    void repositoryFindByStatusShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration downService = createServiceRegistration(601);
        downService.setStatus(ServiceStatus.DOWN);
        serviceRegistrationApi.save(downService);

        List<ServiceRegistration> upServices = serviceRegistrationRepository.findByStatus(ServiceStatus.UP);
        Assertions.assertTrue(upServices.size() >= 2); // at least payment-service instances

        List<ServiceRegistration> downServices = serviceRegistrationRepository.findByStatus(ServiceStatus.DOWN);
        Assertions.assertTrue(downServices.size() >= 1);
    }

    @Order(17)
    @Test
    void repositoryUpdateHeartbeatShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Date newHeartbeat = new Date();
        serviceRegistrationRepository.updateHeartbeat("payment-service", "instance-501", newHeartbeat);

        ServiceRegistration updated = serviceRegistrationRepository.findByServiceNameAndInstanceId("payment-service", "instance-501");
        Assertions.assertNotNull(updated.getLastHeartbeat());
        // Heartbeat should be recent (within 1 second)
        long diff = Math.abs(updated.getLastHeartbeat().getTime() - newHeartbeat.getTime());
        Assertions.assertTrue(diff < 1000);
    }

    @Order(18)
    @Test
    void repositoryUpdateStatusShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        serviceRegistrationRepository.updateStatus("payment-service", "instance-501", ServiceStatus.OUT_OF_SERVICE);
        ServiceRegistration updated = serviceRegistrationRepository.findByServiceNameAndInstanceId("payment-service", "instance-501");
        Assertions.assertEquals(ServiceStatus.OUT_OF_SERVICE, updated.getStatus());
    }

    // ========== NEW TESTS: SystemApi Methods ==========

    @Inject
    @Setter
    private ServiceRegistrationSystemApi serviceRegistrationSystemApi;

    @Order(19)
    @Test
    void systemApiRegisterInternalShouldBypassPermissions() {
        // System API should work without impersonation
        ServiceRegistration service = createServiceRegistration(701);
        service.setServiceName("internal-service");
        ServiceRegistration registered = serviceRegistrationSystemApi.registerInternal(service);
        Assertions.assertNotNull(registered);
        Assertions.assertTrue(registered.getId() > 0);
    }

    @Order(20)
    @Test
    void systemApiUpdateStatusShouldWork() {
        serviceRegistrationSystemApi.updateStatus("internal-service", "instance-701", ServiceStatus.DOWN);
        ServiceRegistration found = serviceRegistrationRepository.findByServiceNameAndInstanceId("internal-service", "instance-701");
        Assertions.assertEquals(ServiceStatus.DOWN, found.getStatus());
    }

    @Order(21)
    @Test
    void systemApiCleanupInactiveServicesShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        // Create a service with old heartbeat
        ServiceRegistration oldService = createServiceRegistration(801);
        oldService.setServiceName("old-service");
        oldService.setLastHeartbeat(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        serviceRegistrationApi.save(oldService);

        // Cleanup services inactive for more than 30 minutes (1800 seconds)
        serviceRegistrationSystemApi.cleanupInactiveServices(1800);

        ServiceRegistration found = serviceRegistrationRepository.findByServiceNameAndInstanceId("old-service", "instance-801");
        Assertions.assertEquals(ServiceStatus.OUT_OF_SERVICE, found.getStatus());
    }

    // ========== NEW TESTS: Heartbeat & Health Check ==========

    @Order(22)
    @Test
    void heartbeatUpdateShouldRefreshTimestamp() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration service = createServiceRegistration(901);
        service = serviceRegistrationApi.save(service);

        Date oldHeartbeat = service.getLastHeartbeat();
        try {
            Thread.sleep(100); // Small delay to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        serviceRegistrationRepository.updateHeartbeat(service.getServiceName(), service.getInstanceId(), new Date());
        ServiceRegistration updated = serviceRegistrationRepository.findByServiceNameAndInstanceId(service.getServiceName(), service.getInstanceId());

        Assertions.assertTrue(updated.getLastHeartbeat().after(oldHeartbeat));
    }

    @Order(23)
    @Test
    void systemApiUpdateHeartbeatInternalShouldReturnFalseWhenMissing() {
        Assertions.assertFalse(serviceRegistrationSystemApi.updateHeartbeatInternal("missing-service", "missing-instance"));
    }

    @Order(24)
    @Test
    void batchHealthCheckShouldReturnServiceStatuses() {
        Map<String, ServiceStatus> statuses = serviceRegistrationSystemApi.performBatchHealthCheck();
        Assertions.assertNotNull(statuses);
        // Should contain at least some services we created
        Assertions.assertTrue(statuses.size() > 0);
    }

    // ========== NEW TESTS: Integration Tests for Service Lifecycle ==========

    @Order(25)
    @Test
    void serviceLifecycleIntegrationTest() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);

        // 1. Register a service
        ServiceRegistration service = createServiceRegistration(1001);
        service.setServiceName("lifecycle-service");
        service = serviceRegistrationApi.save(service);
        Assertions.assertEquals(ServiceStatus.UP, service.getStatus());

        // 2. Update heartbeat
        Date heartbeat = new Date();
        serviceRegistrationRepository.updateHeartbeat("lifecycle-service", "instance-1001", heartbeat);
        ServiceRegistration afterHeartbeat = serviceRegistrationRepository.findByServiceNameAndInstanceId("lifecycle-service", "instance-1001");
        Assertions.assertNotNull(afterHeartbeat.getLastHeartbeat());

        // 3. Update status to DOWN
        serviceRegistrationSystemApi.updateStatus("lifecycle-service", "instance-1001", ServiceStatus.DOWN);
        ServiceRegistration afterDown = serviceRegistrationRepository.findByServiceNameAndInstanceId("lifecycle-service", "instance-1001");
        Assertions.assertEquals(ServiceStatus.DOWN, afterDown.getStatus());

        // 4. Deregister (remove)
        serviceRegistrationApi.remove(service.getId());
        ServiceRegistration deleted = serviceRegistrationRepository.findByServiceNameAndInstanceId("lifecycle-service", "instance-1001");
        Assertions.assertNull(deleted);
    }

    @Order(26)
    @Test
    void multipleInstancesOfSameServiceShouldCoexist() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);

        ServiceRegistration instance1 = createServiceRegistration(1101);
        instance1.setServiceName("multi-instance-service");
        instance1.setInstanceId("multi-instance-1");

        ServiceRegistration instance2 = createServiceRegistration(1102);
        instance2.setServiceName("multi-instance-service");
        instance2.setInstanceId("multi-instance-2");

        serviceRegistrationApi.save(instance1);
        serviceRegistrationApi.save(instance2);

        List<ServiceRegistration> instances = serviceRegistrationRepository.findByServiceName("multi-instance-service");
        Assertions.assertEquals(2, instances.size());
    }

    @Order(27)
    @Test
    void deregisterByKeyShouldBeIdempotent() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        ServiceRegistration service = createServiceRegistration(1201);
        service.setServiceName("idempotent-service");
        service.setInstanceId("idempotent-instance");
        serviceRegistrationApi.save(service);

        Assertions.assertDoesNotThrow(() -> serviceRegistrationSystemApi.deregisterByKey("idempotent-service", "idempotent-instance"));
        Assertions.assertDoesNotThrow(() -> serviceRegistrationSystemApi.deregisterByKey("idempotent-service", "idempotent-instance"));
        Assertions.assertNull(serviceRegistrationRepository.findByServiceNameAndInstanceId("idempotent-service", "idempotent-instance"));
    }

    private ServiceRegistration createServiceRegistration(int seed) {
        ServiceRegistration entity = new ServiceRegistration(
                "service-" + seed,
                "1.0." + seed,
                "instance-" + seed,
                "http://localhost:" + (8080 + seed),
                "http",
                ServiceStatus.UP
        );
        entity.setDescription("Test service " + seed);
        return entity;
    }
}
