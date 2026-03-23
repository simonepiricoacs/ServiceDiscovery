# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check ServiceRegistration Rest Api Response

  Scenario: ServiceRegistration CRUD Operations

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/api/serviceregistration'
    # ---- Add entity fields here -----
    And request
    """ {
      "serviceName": "test-service",
      "serviceVersion": "1.0.0",
      "instanceId": "test-instance-1",
      "endpoint": "http://localhost:8080",
      "protocol": "http",
      "status": "UP",
      "description": "Test service for Karate"
    } """
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":1,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "serviceName": 'test-service',
        "serviceVersion": '1.0.0',
        "instanceId": 'test-instance-1',
        "endpoint": 'http://localhost:8080',
        "protocol": 'http',
        "status": 'UP',
        "description": 'Test service for Karate',
        "tags": '##[]',
        "metadata": '##object',
        "lastHeartbeat": '##number',
        "healthCheckInterval": '#null',
        "healthCheckEndpoint": '#null',
        "configuration": '#null'
       }
    """
    * def entityId = response.id
    
    # --------------- UPDATE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/api/serviceregistration'
    # ---- Add entity fields here -----
    And request
    """ {
          "id":"#(entityId)",
          "entityVersion":1,
          "serviceName": "test-service",
          "serviceVersion": "1.0.0",
          "instanceId": "test-instance-1",
          "endpoint": "http://localhost:9090",
          "protocol": "http",
          "status": "UP"
    }
    """
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "serviceName": 'test-service',
        "serviceVersion": '1.0.0',
        "instanceId": 'test-instance-1',
        "endpoint": 'http://localhost:9090',
        "protocol": 'http',
        "status": 'UP',
        "description": '#null',
        "tags": '##[]',
        "metadata": '##object',
        "lastHeartbeat": '##number',
        "healthCheckInterval": '#null',
        "healthCheckEndpoint": '#null',
        "configuration": '#null'
       }
    """

  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/api/serviceregistration/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "serviceName": 'test-service',
        "serviceVersion": '1.0.0',
        "instanceId": 'test-instance-1',
        "endpoint": 'http://localhost:9090',
        "protocol": 'http',
        "status": 'UP',
        "description": '#null',
        "tags": '##[]',
        "metadata": '##object',
        "lastHeartbeat": '##number',
        "healthCheckInterval": '#null',
        "healthCheckEndpoint": '#null',
        "configuration": '#null'
       }
    """

  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/api/serviceregistration'
    When method GET
    Then status 200
    And match response.results contains deep
    """
      {
        "serviceName": 'test-service',
        "instanceId": 'test-instance-1',
        "endpoint": 'http://localhost:9090'
      }
    """

  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/api/serviceregistration/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
