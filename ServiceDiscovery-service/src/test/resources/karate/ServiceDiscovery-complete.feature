# Complete ServiceDiscovery REST API Tests
# Tests all CRUD operations and custom endpoints
# Note: JWT authentication is disabled in test environment

Feature: Complete ServiceDiscovery REST API Tests

  # ========================================
  # CRUD OPERATIONS
  # ========================================

  Scenario: Create a new ServiceRegistration
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And header Accept = 'application/json'
    And request
      """
      {
        "serviceName": "payment-service",
        "serviceVersion": "1.0.0",
        "instanceId": "payment-001",
        "endpoint": "http://localhost:8081",
        "protocol": "http",
        "status": "UP",
        "description": "Payment Service Instance 1"
      }
      """
    When method POST
    Then status 200
    And match response.id == '#number'
    And match response.serviceName == 'payment-service'
    And match response.instanceId == 'payment-001'
    And match response.endpoint == 'http://localhost:8081'
    And match response.status == 'UP'
    And match response.entityVersion == 1
    * def serviceId = response.id

    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    When method DELETE
    Then status 204

  Scenario: Update an existing ServiceRegistration
    # First create a service
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "user-service",
        "serviceVersion": "2.0.0",
        "instanceId": "user-001",
        "endpoint": "http://localhost:8082",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def createdService = response
    * def serviceId = createdService.id

    # Now update it
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "id": #(serviceId),
        "entityVersion": 1,
        "serviceName": "user-service",
        "serviceVersion": "2.0.0",
        "instanceId": "user-001",
        "endpoint": "http://localhost:9092",
        "protocol": "https",
        "status": "UP"
      }
      """
    When method PUT
    Then status 200
    And match response.id == serviceId
    And match response.endpoint == 'http://localhost:9092'
    And match response.protocol == 'https'
    And match response.entityVersion == 2

    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    When method DELETE
    Then status 204

  Scenario: Find ServiceRegistration by ID
    # First create a service
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "notification-service",
        "serviceVersion": "1.0.0",
        "instanceId": "notif-001",
        "endpoint": "http://localhost:8083",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def serviceId = response.id

    # Now find it by ID
    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    And header Accept = 'application/json'
    When method GET
    Then status 200
    And match response.id == serviceId
    And match response.serviceName == 'notification-service'
    And match response.instanceId == 'notif-001'

    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    When method DELETE
    Then status 204

  Scenario: Find All ServiceRegistrations
    # Create multiple services
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "order-service",
        "serviceVersion": "1.0.0",
        "instanceId": "order-001",
        "endpoint": "http://localhost:8084",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def firstServiceId = response.id

    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "order-service",
        "serviceVersion": "1.0.0",
        "instanceId": "order-002",
        "endpoint": "http://localhost:8085",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def secondServiceId = response.id

    # Now find all
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Accept = 'application/json'
    When method GET
    Then status 200
    And match response.results == '#array'
    And assert response.results.length >= 2
    And match each response.results contains { serviceName: '#string', instanceId: '#string' }

    Given url serviceBaseUrl + '/water/serviceregistration/' + firstServiceId
    When method DELETE
    Then status 204

    Given url serviceBaseUrl + '/water/serviceregistration/' + secondServiceId
    When method DELETE
    Then status 204

  Scenario: Delete ServiceRegistration
    # First create a service
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "temp-service",
        "serviceVersion": "1.0.0",
        "instanceId": "temp-001",
        "endpoint": "http://localhost:8099",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def serviceId = response.id

    # Now delete it
    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    When method DELETE
    Then status 204

    # Verify it's deleted
    Given url serviceBaseUrl + '/water/serviceregistration/' + serviceId
    When method GET
    Then status 404

  # ========================================
  # VALIDATION TESTS
  # ========================================

  Scenario: Create ServiceRegistration with missing required fields should fail
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "incomplete-service"
      }
      """
    When method POST
    Then status 422

  Scenario: Create duplicate ServiceRegistration should fail
    # Create first service
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "unique-service",
        "serviceVersion": "1.0.0",
        "instanceId": "unique-001",
        "endpoint": "http://localhost:8090",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 200
    * def duplicateServiceId = response.id

    # Try to create duplicate (same serviceName + instanceId)
    Given url serviceBaseUrl + '/water/serviceregistration'
    And header Content-Type = 'application/json'
    And request
      """
      {
        "serviceName": "unique-service",
        "serviceVersion": "1.0.0",
        "instanceId": "unique-001",
        "endpoint": "http://localhost:8091",
        "protocol": "http",
        "status": "UP"
      }
      """
    When method POST
    Then status 409

    Given url serviceBaseUrl + '/water/serviceregistration/' + duplicateServiceId
    When method DELETE
    Then status 204

  # ========================================
  # AUTHORIZATION TESTS
  # ========================================
  # Note: Authorization tests are disabled since JWT validation is turned off in test environment
  # To enable authorization tests, set water.rest.security.jwt.validate=true in application.properties
