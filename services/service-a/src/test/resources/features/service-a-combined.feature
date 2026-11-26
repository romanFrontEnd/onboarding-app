@ServiceA
Feature: Combined greeting from service-a
  As a developer or consumer of service-a
  I want to request a combined greeting
  So that I can see service-a and service-b working together

  Background:
    Given service-a is running
    And SERVICE_B_URL, SERVICE_B_TIMEOUT and SERVICE_B_RETRIES are set through configuration

  @HappyPath @SmokeTest @SC-001
  Scenario: Combined result when service-b answers in time
    Given service-b answers GET "/api/v1/hello" for name "Roman" with status 200
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response status is 200
    And the response contains the "service-a" block
    And the response contains the "service-b" block with "message" and "version"

  @ErrorCase @SC-002
  Scenario: Missing name is rejected
    When I send GET "/api/v1/combined" to service-a
    Then the response status is 400
    And the error code is "INVALID_REQUEST"

  @ErrorCase @Wiremock @SC-003
  Scenario: Service-b answers slower than the configured timeout
    Given service-b answers after more than SERVICE_B_TIMEOUT
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response status is 503
    And the error code is "SERVICE_B_UNAVAILABLE"

  @ErrorCase @Wiremock @SC-004
  Scenario: Service-b refuses the connection
    Given service-b refuses the connection
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response status is 503
    And the error code is "SERVICE_B_UNAVAILABLE"

  @ErrorCase @Cluster @SC-019
  Scenario: Service-b is scaled to zero in the cluster
    Given service-b has no running replicas
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response status is 503
    And the error code is "SERVICE_B_UNAVAILABLE"

  @ErrorCase @SC-005
  Scenario: Service-b returns a server error and service-a does not retry
    Given service-b answers GET "/api/v1/hello" with status 500
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then service-a calls service-b exactly once
    And the response status is 502
    And the error code is "SERVICE_B_ERROR"

  @ErrorCase @Wiremock @SC-006
  Scenario: Service-a retries timeouts up to the configured count
    Given service-b answers after more than SERVICE_B_TIMEOUT
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then service-a calls service-b SERVICE_B_RETRIES plus 1 times in total
    And the response status is 503

  @HappyPath @Wiremock @SC-016
  Scenario: Service-a recovers when service-b answers on a retry
    Given the first call to service-b times out
    And the second call to service-b answers with status 200
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response status is 200
    And the response contains the "service-b" block with "message" and "version"

  @EdgeCase @Wiremock @SC-017
  Scenario: The response time is bounded when service-b is down
    Given service-b answers after more than SERVICE_B_TIMEOUT
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then the response is returned within SERVICE_B_RETRIES plus 1 times SERVICE_B_TIMEOUT plus the backoff

  @HappyPath @SC-007
  Scenario: The transaction ID from the request is passed to service-b
    Given the request has transaction ID "abc-123"
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then service-b receives "X-Transaction-Id" with the value "abc-123"
    And the logs of service-a and service-b both contain "abc-123"

  @HappyPath @SC-018
  Scenario: A transaction ID is generated when the request has none
    Given the request has no transaction ID
    When I send GET "/api/v1/combined?name=Roman" to service-a
    Then service-a generates a transaction ID
    And service-b receives that same value in "X-Transaction-Id"
    And the logs of service-a and service-b both contain that value

  @EdgeCase @Parameterized @SC-008
  Scenario Outline: Name length boundary
    When I send GET "/api/v1/combined" to service-a with a name of <length> characters
    Then the response status is <status>

    Examples:
      | length | status |
      | 1      | 200    |
      | 50     | 200    |
      | 51     | 400    |

  @HappyPath @SC-009
  Scenario: Readiness of service-a does not depend on service-b
    Given service-b has no running replicas
    When I send GET "/q/health/ready" to service-a
    Then the response status is 200
