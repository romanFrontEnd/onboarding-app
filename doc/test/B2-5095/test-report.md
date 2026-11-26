# Test Report — B2-5095: Two Quarkus services on local minikube via ArgoCD

---
- change_id: "B2-5095"
- spec_ref: "doc/spec/B2-5095/spec.md"
- status: "approved"
- reported_by: "Teddy"
- report_date: "2026-11-25"
- commit_sha: "b5e3eda"
- suite_command: "JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home mvn test (run in services/service-a and services/service-b)"
- framework: "quarkus"
---

## 1. What was tested

| Test class | Layer | Type | Source class under test | Tests |
|------------|-------|------|-------------------------|-------|
| service-b `CucumberRunnerTest` (service-b-hello.feature) | web | BDD | `HelloRestController`, `HelloService` | 12 |
| service-b `HelloServiceTest` | service | unit | `HelloService` | 1 |
| service-b `ErrorCodeTest` | exception | unit | `ErrorCode` | 5 |
| service-a `CucumberRunnerTest` (service-a-combined.feature) | web | BDD with WireMock | `CombinedRestController`, `CombinedService`, `ServiceBClientAdapter` | 20 |
| service-a `NameValidatorTest` | businessrules | unit | `NameValidator` | 7 |
| service-a `CombinedServiceTest` | service | unit (mocked adapter and validator) | `CombinedService` | 3 |
| service-a `AbstractBackendExceptionMapperTest` | client | unit (mocked `Response`) | `AbstractBackendExceptionMapper`, `ServiceBExceptionMapper` | 6 |
| service-a `ErrorCodeTest` | exception | unit | `ErrorCode` | 14 |
| service-a `BsiExceptionFunctionTest` | function | unit | `BsiExceptionFunction` | 7 |

The Cucumber runner counts scenario rows (outline examples included) and the Background steps.

## 2. Results

```
service-b: Tests run: 18, Passed: 18, Failed: 0, Skipped: 0
service-a: Tests run: 57, Passed: 57, Failed: 0, Skipped: 0
Total:     Tests run: 75, Passed: 75, Failed: 0, Skipped: 0
```

| Test class | Run | Passed | Failed | Notes |
|------------|-----|--------|--------|-------|
| service-b CucumberRunnerTest | 12 | 12 | 0 | 7 scenarios (SC-010, 011, 021, 022 with outline rows) |
| service-b HelloServiceTest | 1 | 1 | 0 | |
| service-b ErrorCodeTest | 5 | 5 | 0 | coverage closure G1 |
| service-a CucumberRunnerTest | 20 | 20 | 0 | 15 scenarios; SC-019 excluded by the `@Cluster` tag |
| service-a NameValidatorTest | 7 | 7 | 0 | |
| service-a CombinedServiceTest | 3 | 3 | 0 | |
| service-a AbstractBackendExceptionMapperTest | 6 | 6 | 0 | coverage closure G4 |
| service-a ErrorCodeTest | 14 | 14 | 0 | coverage closure G2 |
| service-a BsiExceptionFunctionTest | 7 | 7 | 0 | coverage closure G3 |

History of the run: the suite was first proven RED (26 failing tests, all `NOT_IMPLEMENTED` or unmet expectations), then GREEN after Cody's implementation. During this verification pass, finding F1 was proven RED first (SC-021 and SC-022 returned 200 instead of 400), then fixed.

## 3. Coverage

### 3.1 Measured coverage

JaCoCo through the `quarkus-jacoco` test dependency, read from `target/jacoco-report/jacoco.csv` of each service. Only classes loaded inside the Quarkus test application are measured, so every coverage test is annotated `@QuarkusTest`.

| Service | Scope | Lines | Branches |
|---------|-------|-------|----------|
| service-b | all instrumented classes (see 3.2) | 153/238 = 64% | 25/60 = 42% |
| service-a | all instrumented classes (see 3.2) | 293/415 = 71% | 76/118 = 64% |

Per class (service-a, the behaviour classes): `NameValidator` 4/4 lines, `CombinedService` 20/23, `ServiceBClientAdapter` 20/23, `BsiExceptionFunction` 20/20, `AbstractBackendExceptionMapper` 32/33, `ErrorCode` 36/37, `BsiException` 23/23, `CombinedRestController` 8/8.
Per class (service-b): `HelloService` 8/8, `HelloRestController` 8/8, `ResteasyReactiveViolationExceptionMapper` 28/29, `ErrorCode` 24/25.

Classes still at 0% lines: service-a `CombinedException`, `CombinedExceptionHandler`, `GenericExceptionHandler`, `ResteasyReactiveViolationExceptionMapper` (see section 5); service-b `HelloException`, `HelloExceptionHandler`, `GenericExceptionHandler`.

### 3.2 Scope & exclusions

| Excluded pattern | Reason (from the pom) | How excluded classes are verified |
|------------------|-----------------------|-----------------------------------|
| none | The pom and `application.properties` configure no JaCoCo include or exclude entries | not applicable |

**Scope statement:** the percentages cover every class JaCoCo instruments in the test application, including Alfie's template infrastructure (exception handlers, logging utilities, observability classes). The Lombok-only generated model classes (`resources` package) do not appear in the report because JaCoCo filters Lombok-generated code. The low overall figures come mostly from template infrastructure the spec gives no behaviour to. The behaviour classes are listed individually above.

### 3.3 Endpoint-exercise table

| Endpoint | Exercising test | Verdict |
|----------|-----------------|---------|
| A `GET /api/v1/combined` | service-a Cucumber SC-001 to SC-008, 016, 017, 018, 020 | covered |
| A `GET /q/health/live` | service-a Cucumber Background step | covered |
| A `GET /q/health/ready` | service-a Cucumber SC-009 | covered |
| B `GET /api/v1/hello` | service-b Cucumber SC-010, 021, 022; `HelloServiceTest` | covered |
| B `GET /q/health/live`, `/q/health/ready` | service-b Cucumber SC-011 | covered |

## 4. Spec conformance matrix

| Scenario ID | Gherkin scenario | Test(s) | Outcome | Verdict |
|-------------|------------------|---------|---------|---------|
| @SC-001 | service-a-combined: Combined result when service-b answers in time | service-a CucumberRunnerTest | PASSED | conforming |
| @SC-002 | service-a-combined: Missing name is rejected | service-a CucumberRunnerTest, NameValidatorTest | PASSED | conforming |
| @SC-003 | service-a-combined: Service-b answers slower than the configured timeout | service-a CucumberRunnerTest, BsiExceptionFunctionTest | PASSED | conforming |
| @SC-004 | service-a-combined: Service-b refuses the connection | service-a CucumberRunnerTest, BsiExceptionFunctionTest | PASSED | conforming |
| @SC-005 | service-a-combined: Service-b returns a server error and service-a does not retry | service-a CucumberRunnerTest, AbstractBackendExceptionMapperTest | PASSED | conforming |
| @SC-006 | service-a-combined: Service-a retries timeouts up to the configured count | service-a CucumberRunnerTest | PASSED | conforming |
| @SC-007 | service-a-combined: The transaction ID from the request is passed to service-b | service-a CucumberRunnerTest; live run 2026-11-25 | PASSED | conforming (live: ID `live-001` found in the logs of both pods) |
| @SC-008 | service-a-combined: Name length boundary (3 rows) | service-a CucumberRunnerTest, NameValidatorTest | PASSED | conforming |
| @SC-009 | service-a-combined: Readiness of service-a does not depend on service-b | service-a CucumberRunnerTest | PASSED | conforming (baseline) |
| @SC-010 | service-b-hello: Greeting for a valid name | service-b CucumberRunnerTest, HelloServiceTest | PASSED | conforming |
| @SC-011 | service-b-hello: Health endpoints answer (2 rows) | service-b CucumberRunnerTest | PASSED | conforming (baseline) |
| @SC-012 | deployment-gitops: A new version is deployed only through a Git change | live run: `release.sh service-b --push` (tag `9c088b9`) | PASSED | conforming (ArgoCD rolled service-b from `b5e3eda` to `9c088b9`, Synced and Healthy, no mutable tag) |
| @SC-013 | deployment-gitops: A tag is never committed before its image is in the cluster | live run: `release.sh` with a failing `minikube image load` | PASSED | conforming (exit 1, HEAD and overlay tag unchanged) |
| @SC-014 | deployment-gitops: A clean cluster reaches Synced and Healthy | live run: `make up` on a new minikube profile | PASSED | conforming (root, service-a, service-b Synced and Healthy, all pods 1/1) |
| @SC-015 | deployment-gitops: The example overlay differs from the base | live run: `kubectl kustomize` diff local vs example | PASSED | conforming (replicas 1 to 2, `QUARKUS_LOG_LEVEL`, service-a timeout 2000 to 3000, other namespace) |
| @SC-016 | service-a-combined: Service-a recovers when service-b answers on a retry | service-a CucumberRunnerTest | PASSED | conforming |
| @SC-017 | service-a-combined: The response time is bounded when service-b is down | service-a CucumberRunnerTest | PASSED | conforming |
| @SC-018 | service-a-combined: A transaction ID is generated when the request has none | service-a CucumberRunnerTest; live run 2026-11-25 | PASSED | conforming (live: response carried a generated `X-Transaction-Id`) |
| @SC-019 | service-a-combined: Service-b is scaled to zero in the cluster | live run: `kubectl scale deploy/service-b --replicas=0` | PASSED | conforming (503 `SERVICE_B_UNAVAILABLE` in 0.48 s, service-a readiness stayed 200), see finding 6 |
| @SC-020 | service-a-combined: Service-b returns a client error and service-a does not retry | service-a CucumberRunnerTest | PASSED | conforming |
| @SC-021 | service-b-hello: Missing name is rejected | service-b CucumberRunnerTest | PASSED | conforming (added with spec v0.10) |
| @SC-022 | service-b-hello: Name length boundary (3 rows) | service-b CucumberRunnerTest | PASSED | conforming (added with spec v0.10) |

## 5. Findings & loop-backs

| # | Finding | Evidence | Loop-back |
|---|---------|----------|-----------|
| 1 | service-b answered `200 "Hello, null"` for a missing or empty `name`, while the OpenAPI contract declares a 400. The spec did not define service-b's validation. | Built jar, `GET /api/v1/hello` and `?name=` both returned 200. SC-021 and SC-022 (51 characters) failed RED before the fix. | Spec defined in v0.10 by the Developer (Mandy's role), fixed by Cody with Bean Validation on the query parameter, now GREEN. **Closed.** |
| 2 | Alfie's template infrastructure is unused or unreachable: service-a `ResteasyReactiveViolationExceptionMapper` (no Bean Validation constraint can trigger it, 0%), the legacy exception handlers `CombinedExceptionHandler`, `HelloExceptionHandler`, `GenericExceptionHandler` and exceptions `CombinedException`, `HelloException` (nothing throws them now), and the RELAY path of `BsiExceptionMapper` (service-a uses NORMALIZE). | JaCoCo report, 0% or 58% lines | Developer decision: keep (the approved plan's choice) or remove. Not actioned. Removal would go to Cody. |
| 3 | SC-007 and SC-018 were only partly provable in the Java suite (service-b's own logs are not visible there). | Live run: transaction ID `live-001` appears in 6 log lines of service-a and 5 of service-b | **Closed** by the live run |
| 4 | SC-012 to SC-015 and SC-019 needed the live environment. | Live run of 2026-11-25 on minikube profile `onboarding` | **Closed**, see section 4 |
| 5 | The retry backoff was undefined in the spec when SC-017 was written. Closed in spec v0.9 (200 ms fixed). | spec v0.9 | **Closed.** |
| 6 | ArgoCD `selfHeal: true` reverts a manual `kubectl scale ... --replicas=0` within seconds, so the "service-b at 0" state used for AC3 is transient. The 503 was observed during that window (no ready endpoint, connection refused). | Live run: replicas back to 1 about 6 s after the scale | Developer decision: keep (GitOps-correct) or pause auto-sync on `service-b` for a longer demo. No change made. |
| 7 | `release.sh --push` pushed `HEAD` to a branch of the same name, but ArgoCD tracks `main`, so a release never reached the cluster. | Found while preparing the live check | Fixed in Confys Local's template (push to `$DEPLOY_BRANCH`, default `main`) and re-rendered. **Closed.** |

## 6. Remaining gaps

- The live run used the author's own machine (Apple Silicon, minikube Docker driver). A second engineer reproducing it from the README alone (AC9) has not happened, and the root README of the repo is not written yet.
- AC10 (private-repo credentials) is not exercised: the repo is public, so ArgoCD needs none.
- The `example` overlay is rendered and diffed but was not deployed.
- Finding 2 is an accepted risk until the Developer decides: about 90 lines of template infrastructure in each service have no behaviour to test.
- `LoggingUtils` and the logging interceptors stay at low coverage (17% to 73%). They are Alfie's templates and the spec states no requirement on them.
- SC-007 and SC-018 are proven only to the extent finding 3 describes.
- None outstanding on approvals: the Analyst (Roman Test) approved the spec on 2026-11-25.

## Run history

| Date | Commit | Suite result | Coverage (scope) | Report change |
|------|--------|--------------|------------------|---------------|
| 2026-11-25 | b5e3eda | GREEN, 75/75 (service-a 57, service-b 18) | service-a 71% lines, service-b 64% lines (all instrumented classes) | initial report |
| 2026-11-25 | 1fa4c2c | Java suite unchanged (75/75). Live run: SC-012 to SC-015 and SC-019 PASSED, SC-007 and SC-018 confirmed live | not re-measured | live verification on minikube, findings 3 and 4 closed, findings 6 and 7 added |
