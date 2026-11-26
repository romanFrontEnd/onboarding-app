# Plan — B2-5095: Two Quarkus services on local minikube via ArgoCD

---
change_id: "B2-5095"
spec_ref: "doc/spec/B2-5095/spec.md"
status: "approved"
planned_by: "alfie"
approved_at: "2026-11-25"
---

## Files touched

Base `services/service-x/` with `x` = `a` or `b`. Package: `com.videotron.bsi.service.x`. Everything is new, and no existing file is modified.

### Both services
| File | Why (pattern) |
|---|---|
| `pom.xml` (OpenAPI variant) | Build, from the template (see pom adjustments below) |
| `.gitignore`, logging constants and utils | Script outputs |
| `src/main/resources/application.properties` | 1A. Service A also gets `SERVICE_B_URL`, `SERVICE_B_TIMEOUT`, `SERVICE_B_RETRIES` |
| `src/main/resources/openapi/...yaml` | Copy of `doc/spec/B2-5095/openapi-service-x.yaml` |
| `constants/Dictionary.java` | 2 |
| `exception/ErrorCode.java`, `ErrorFamily.java` | 3. A: `INVALID_REQUEST`, `SERVICE_B_UNAVAILABLE`, `SERVICE_B_ERROR`. B: `INVALID_REQUEST` |
| `model/HeaderParameter.java` | 4, for `X-Transaction-Id` |
| `utils/HeaderUtils.java` | 5, if still applicable per its status note |
| `web/HelloRestController.java` (B), `web/CombinedRestController.java` (A) | 6A and 9A |
| `service/HelloService.java` (B), `service/CombinedService.java` (A) | 8A, shell with Not Implemented |
| `exception/ResteasyReactiveViolationExceptionMapper.java` | 31 |
| `EnableObserved`, `ObservedInterceptor`, `ObservationRegistryProducer` | 32, local CDI observed mode |
| `README.md` (French, per Alfie's rule) | 30 |

### Service A only
| File | Why |
|---|---|
| `mapping/CombinedMapper.java` | 24, MapStruct shell for the combined response |
| `businessrules/NameValidator.java` | 25, `name` rule (non-blank, at most 50) |
| `client/serviceb/ServiceBClient.java`, `ServiceBRequestHeader.java` | 33 |
| `client/mapper/AbstractBackendExceptionMapper.java`, `client/serviceb/ServiceBExceptionMapper.java` | 34 |
| `exception/BsiException.java`, `ExternalServiceException.java`, `BsiExceptionMapper.java` | 35 |
| `function/BsiExceptionFunction.java`, `client/serviceb/ServiceBClientAdapter.java` | 36, imperative variant (blocking client) |

### Not generated
- Pattern 26 (REST client shell) overlaps with 33-36 on service A.
- `pipeline.yaml`: corporate Tekton and native build, and hosted CI/CD is out of scope.
- The template Dockerfile: internal Nexus image and native binary. A JVM-mode Dockerfile (public `eclipse-temurin:21-jre`, multi-arch) is written by Cody.
- `Makefile`, `scripts/release.sh`, `deploy/` manifests and ArgoCD Applications: Confy and the developer.

### Pom adjustments after generation (logged under Deviations)
- Remove or define the Nexus repository entries.
- Drop Agroal and Oracle JDBC (no database).
- SmallRye Fault Tolerance is not in the pom. Needed for retry and timeout. Hand-off to Cody.

## Order of work

1. Create the structure for service-b: folders, pom, `.gitignore` and logging only, no `pipeline` or `dockerfile`. (parallel with 2)
2. Create the structure for service-a, the same way. (parallel with 1)
3. Copy the OpenAPI files, then generate the shell components for service-b. (independent of A)
4. Generate the shell components for service-a.
5. Apply the pom adjustments (sequential, after the structures exist).
6. `validate-shell` on both: `mvn compile` with `JAVA_HOME` set to Zulu 21.
7. `present-summary` at the `shell-approved` gate. Commit only on approval.

## Tests that prove it

- `mvn compile` passes on both services with no business logic: every method body is Not Implemented.
- No test files from Alfie. Teddy writes the RED suites next, from SC-001 to SC-019. The `@Wiremock` scenarios become unit tests on service A's client. The `@Cluster` scenarios are demonstrated later.

## Risks

- Maven resolution of Quarkus 3.40.1.1 needs Maven Central. The Nexus placeholders must go first. Maven 3.10.2 is installed, and `JAVA_HOME` must point to Zulu 21 (shell default is 17).
- The OpenAPI generator can create duplicate classes. `copy-openapi-classes --disable-plugin` is the fallback.
- Package names `com.videotron.bsi.service.a.service` are awkward but valid. Names kept for traceability with the ticket, spec and Gherkin.
- Riskiest step: 6 (first real compile). Rollback: delete `services/service-a` and `services/service-b`. Nothing existing is modified.

## Rejected options

- A hand-written controller without OpenAPI (needs hand-made DTOs).
- The template Dockerfile and `pipeline.yaml` (internal and native, wrong for a local demo).
- One shared module for both services (the ticket asks for two services).

## Deviations

Empty until implementation starts.
