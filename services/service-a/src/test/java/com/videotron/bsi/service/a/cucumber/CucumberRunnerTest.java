package com.videotron.bsi.service.a.cucumber;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.videotron.bsi.service.a.cucumber",
    tags = "not @Cluster",
    plugin = {"pretty", "json:target/cucumber-reports/cucumber.json", "html:target/cucumber-reports/cucumber.html"}
)
public class CucumberRunnerTest extends CucumberQuarkusTest {
}
