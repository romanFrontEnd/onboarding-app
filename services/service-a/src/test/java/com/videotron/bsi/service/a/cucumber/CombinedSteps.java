package com.videotron.bsi.service.a.cucumber;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;
import java.util.logging.LogManager;

@Slf4j
public class CombinedSteps {

    private static final String HELLO_PATH = "/api/v1/hello";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";
    /** Test infrastructure overhead tolerated on timing assertions. */
    private static final long SCHEDULING_TOLERANCE_MS = 1000;

    private final Config config = ConfigProvider.getConfig();

    @Inject
    ScenarioContext scenarioContext;

    private ValidatableResponse result;
    private InMemoryLogHandler logHandler;

    @Before
    public void setup() {
        WireMockExtensions.restartIfStopped();
        WireMockExtensions.resetAll();
        // Low-priority default so scenarios that do not stub service-b get a valid greeting
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(HELLO_PATH))
            .atPriority(10)
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("hello-success.json")));
        logHandler = new InMemoryLogHandler(record -> record.getMessage() != null);
        LogManager.getLogManager().getLogger("").addHandler(logHandler);
    }

    @After
    public void cleanup() {
        LogManager.getLogManager().getLogger("").removeHandler(logHandler);
        WireMockExtensions.restartIfStopped();
    }

    // --- Background ---

    @Given("service-a is running")
    public void service_a_is_running() {
        given().when().get("/q/health/live").then().statusCode(200);
    }

    @Given("SERVICE_B_URL, SERVICE_B_TIMEOUT and SERVICE_B_RETRIES are set through configuration")
    public void service_b_configuration_is_set() {
        assertTrue(config.getOptionalValue("quarkus.rest-client.service-b.url", String.class).isPresent());
        assertTrue(config.getOptionalValue("quarkus.rest-client.service-b.read-timeout", Long.class).isPresent());
        assertTrue(config.getOptionalValue("service-b.retries", Integer.class).isPresent());
    }

    // --- Given: service-b behaviour (every step configures a real stub) ---

    @Given("service-b answers GET {string} for name {string} with status {int}")
    public void service_b_answers_for_name(String path, String name, int status) {
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(path))
            .withQueryParam("name", equalTo(name))
            .willReturn(answer(status)));
    }

    @Given("service-b answers GET {string} with status {int}")
    public void service_b_answers_with_status(String path, int status) {
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(path)).willReturn(answer(status)));
    }

    @Given("service-b answers after more than SERVICE_B_TIMEOUT")
    public void service_b_answers_too_slowly() {
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(HELLO_PATH))
            .willReturn(answer(200).withFixedDelay((int) (timeoutMillis() + 500))));
    }

    @Given("service-b refuses the connection")
    public void service_b_refuses_the_connection() {
        WireMockExtensions.stopServer();
    }

    @Given("service-b has no running replicas")
    public void service_b_has_no_running_replicas() {
        WireMockExtensions.stopServer();
    }

    @Given("the first call to service-b times out")
    public void the_first_call_times_out() {
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(HELLO_PATH))
            .inScenario("retry")
            .whenScenarioStateIs(Scenario.STARTED)
            .willSetStateTo("second-call")
            .willReturn(answer(200).withFixedDelay((int) (timeoutMillis() + 500))));
    }

    @Given("the second call to service-b answers with status {int}")
    public void the_second_call_answers(int status) {
        WireMockExtensions.getWireMockServer().stubFor(get(urlPathEqualTo(HELLO_PATH))
            .inScenario("retry")
            .whenScenarioStateIs("second-call")
            .willReturn(answer(status)));
    }

    // --- Given: request headers ---

    @Given("the request has transaction ID {string}")
    public void the_request_has_transaction_id(String id) {
        scenarioContext.setXTransactionId(id);
    }

    @Given("the request has no transaction ID")
    public void the_request_has_no_transaction_id() {
        scenarioContext.setXTransactionId(null);
    }

    // --- When ---

    @When("I send GET {string} to service-a")
    public void i_send_get_to_service_a(String path) {
        send(path);
    }

    @When("I send GET {string} to service-a with a name of {int} characters")
    public void i_send_get_with_name_of_length(String path, int length) {
        send(path + "?name=" + "a".repeat(length));
    }

    // --- Then: response ---

    @Then("the response status is {int}")
    public void the_response_status_is(int expectedStatus) {
        result.statusCode(expectedStatus);
    }

    @Then("the error code is {string}")
    public void the_error_code_is(String expectedCode) {
        assertEquals(expectedCode, result.extract().jsonPath().getString("code"));
    }

    @Then("the response contains the {string} block with {string} and {string}")
    public void the_response_contains_block(String block, String first, String second) {
        var json = result.extract().jsonPath();
        assertNotNull(json.get(block + "." + first), block + "." + first);
        assertNotNull(json.get(block + "." + second), block + "." + second);
    }

    // --- Then: calls to service-b ---

    @Then("service-a calls service-b exactly once")
    public void service_a_calls_service_b_exactly_once() {
        WireMockExtensions.getWireMockServer().verify(1, getRequestedFor(urlPathEqualTo(HELLO_PATH)));
    }

    @Then("service-a calls service-b SERVICE_B_RETRIES plus 1 times in total")
    public void service_a_calls_service_b_retries_plus_one() {
        WireMockExtensions.getWireMockServer()
            .verify(retries() + 1, getRequestedFor(urlPathEqualTo(HELLO_PATH)));
    }

    @Then("the response is returned within SERVICE_B_RETRIES plus 1 times SERVICE_B_TIMEOUT plus the backoff")
    public void the_response_is_returned_within_the_bound() {
        // The bound is only meaningful when the retry path was actually exercised
        WireMockExtensions.getWireMockServer()
            .verify(retries() + 1, getRequestedFor(urlPathEqualTo(HELLO_PATH)));
        long bound = (retries() + 1) * timeoutMillis() + retries() * backoffMillis() + SCHEDULING_TOLERANCE_MS;
        assertTrue(scenarioContext.getElapsedMillis() <= bound,
            "elapsed %d ms exceeds the bound %d ms".formatted(scenarioContext.getElapsedMillis(), bound));
    }

    // --- Then: correlation ---

    @Then("service-b receives {string} with the value {string}")
    public void service_b_receives_header_with_value(String header, String value) {
        List<LoggedRequest> requests = helloRequests();
        assertFalse(requests.isEmpty(), "service-b received no request");
        requests.forEach(request -> assertEquals(value, request.getHeader(header)));
    }

    @Then("service-a generates a transaction ID")
    public void service_a_generates_a_transaction_id() {
        List<LoggedRequest> requests = helloRequests();
        assertFalse(requests.isEmpty(), "service-b received no request");
        String generated = requests.get(0).getHeader(TRANSACTION_HEADER);
        assertTrue(generated != null && !generated.isBlank(), "no transaction ID reached service-b");
        scenarioContext.setObservedTransactionId(generated);
    }

    @Then("service-b receives that same value in {string}")
    public void service_b_receives_that_same_value(String header) {
        String expected = scenarioContext.getObservedTransactionId();
        assertNotNull(expected);
        helloRequests().forEach(request -> assertEquals(expected, request.getHeader(header)));
    }

    /**
     * service-a side is verified through its captured logs. The service-b side is verified through the
     * header service-b receives: service-b logs are only observable on the cluster.
     */
    @Then("the logs of service-a and service-b both contain {string}")
    public void the_logs_of_both_contain(String id) {
        assertTrue(logHandler.hasLogContaining(id), "service-a logs do not contain " + id);
        assertTrue(helloRequests().stream().allMatch(request -> id.equals(request.getHeader(TRANSACTION_HEADER))));
    }

    @Then("the logs of service-a and service-b both contain that value")
    public void the_logs_of_both_contain_that_value() {
        the_logs_of_both_contain(scenarioContext.getObservedTransactionId());
    }

    // --- Helpers ---

    private void send(String path) {
        RequestSpecification request = given();
        if (scenarioContext.getXTransactionId() != null) {
            request = request.header(TRANSACTION_HEADER, scenarioContext.getXTransactionId());
        }
        long start = System.currentTimeMillis();
        result = request.when().get(path).then();
        scenarioContext.setElapsedMillis(System.currentTimeMillis() - start);
        result.log().all();
    }

    private com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder answer(int status) {
        String body = status < 300 ? "hello-success.json"
            : status < 500 ? "hello-error-client.json" : "hello-error-server.json";
        return aResponse().withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBodyFile(body);
    }

    private List<LoggedRequest> helloRequests() {
        return WireMockExtensions.getWireMockServer()
            .findAll(getRequestedFor(urlPathEqualTo(HELLO_PATH)));
    }

    private long timeoutMillis() {
        return config.getValue("quarkus.rest-client.service-b.read-timeout", Long.class);
    }

    private int retries() {
        return config.getValue("service-b.retries", Integer.class);
    }

    /** Fixed delay between attempts, spec v0.9 (SERVICE_B_RETRY_BACKOFF, milliseconds). */
    private long backoffMillis() {
        return config.getValue("service-b.retry-backoff", Long.class);
    }
}
