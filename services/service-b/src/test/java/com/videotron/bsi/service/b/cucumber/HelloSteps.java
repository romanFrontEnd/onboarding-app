package com.videotron.bsi.service.b.cucumber;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloSteps {

    private ValidatableResponse result;

    @When("I send GET {string} to service-b")
    public void i_send_get_to_service_b(String path) {
        result = given().when().get(path).then();
        result.log().all();
    }

    @Then("the response status is {int}")
    public void the_response_status_is(int expectedStatus) {
        result.statusCode(expectedStatus);
    }

    @Then("the response contains {string}, {string} and {string}")
    public void the_response_contains(String first, String second, String third) {
        var json = result.extract().jsonPath();
        assertNotNull(json.get(first), first);
        assertNotNull(json.get(second), second);
        assertNotNull(json.get(third), third);
    }

    @Then("the greeting message is {string}")
    public void the_greeting_message_is(String expected) {
        assertEquals(expected, result.extract().jsonPath().getString("message"));
    }
}
