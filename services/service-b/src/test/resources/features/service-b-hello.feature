@ServiceB
Feature: Greeting from service-b
  As service-a
  I want to call service-b for a greeting
  So that service-a can build its combined result

  @HappyPath @SmokeTest @SC-010
  Scenario: Greeting for a valid name
    When I send GET "/api/v1/hello?name=Roman" to service-b
    Then the response status is 200
    And the response contains "message", "service" and "version"

  @HappyPath @Parameterized @SC-011
  Scenario Outline: Health endpoints answer
    When I send GET "<endpoint>" to service-b
    Then the response status is 200

    Examples:
      | endpoint        |
      | /q/health/live  |
      | /q/health/ready |

  @ErrorCase @SC-021
  Scenario: Missing name is rejected
    When I send GET "/api/v1/hello" to service-b
    Then the response status is 400
    And the error code is "INVALID_REQUEST"

  @EdgeCase @Parameterized @SC-022
  Scenario Outline: Name length boundary
    When I send GET "/api/v1/hello" to service-b with a name of <length> characters
    Then the response status is <status>

    Examples:
      | length | status |
      | 1      | 200    |
      | 50     | 200    |
      | 51     | 400    |
