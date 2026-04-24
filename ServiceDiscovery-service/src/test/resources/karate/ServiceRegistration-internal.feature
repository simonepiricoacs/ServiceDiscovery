# Internal ServiceRegistration endpoints must work without JWT and must not expose CRUD listing.
Feature: Internal ServiceRegistration API

  Scenario: Internal register heartbeat and deregister
    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl + '/water/internal/serviceregistration/register'
    And request
    """
    {
      "serviceName": "internal-test-service",
      "serviceVersion": "1.0.0",
      "instanceId": "internal-instance-1",
      "endpoint": "http://localhost:9081/water/assetcategories",
      "protocol": "http"
    }
    """
    When method POST
    Then status 200
    And match response.serviceName == 'internal-test-service'
    And match response.instanceId == 'internal-instance-1'
    And match response.status == 'UP'

    Given url serviceBaseUrl + '/water/internal/serviceregistration/heartbeat/internal-test-service/internal-instance-1'
    When method PUT
    Then status 204

    Given url serviceBaseUrl + '/water/internal/serviceregistration/internal-test-service/internal-instance-1'
    When method DELETE
    Then status 204

    Given url serviceBaseUrl + '/water/internal/serviceregistration/internal-test-service/internal-instance-1'
    When method DELETE
    Then status 204

    Given url serviceBaseUrl + '/water/internal/serviceregistration/heartbeat/internal-test-service/internal-instance-1'
    When method PUT
    Then status 404

  Scenario: Internal available listing is exposed but generic CRUD listing is not
    * def suffix = java.util.UUID.randomUUID() + ''
    * def serviceName = 'internal-available-service-' + suffix
    * def instanceId = 'internal-available-instance-' + suffix
    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl + '/water/internal/serviceregistration/register'
    And request { serviceName: '#(serviceName)', serviceVersion: '1.0.0', instanceId: '#(instanceId)', endpoint: 'http://localhost:9082/water/assettags', protocol: 'http' }
    When method POST
    Then status 200

    Given url serviceBaseUrl + '/water/internal/serviceregistration/available'
    When method GET
    Then status 200
    And match response == '#array'
    And match response contains deep
    """
    {
      serviceName: '#(serviceName)',
      instanceId: '#(instanceId)',
      status: 'UP'
    }
    """

    Given url serviceBaseUrl + '/water/internal/serviceregistration'
    When method GET
    Then assert responseStatus == 404 || responseStatus == 405 || responseStatus == 500
