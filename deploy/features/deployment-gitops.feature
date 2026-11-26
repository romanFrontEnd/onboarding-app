@Deployment
Feature: GitOps deployment on the local cluster
  As a developer
  I want deployments driven only by Git changes
  So that the environment is reproducible and auditable

  Background:
    Given the minikube cluster is running
    And ArgoCD is installed at the pinned version

  @HappyPath @SmokeTest @SC-012
  Scenario: A new version is deployed only through a Git change
    Given the image "service-a:<new-sha>" is loaded into the cluster
    When the tag "<new-sha>" is committed and pushed to the repository
    Then ArgoCD syncs service-a to Healthy and Synced
    And no mutable tag was re-pushed

  @ErrorCase @SC-013
  Scenario: A tag is never committed before its image is in the cluster
    Given the image "service-a:<new-sha>" is not loaded into the cluster
    When I run release.sh for service-a
    Then the script stops before committing the tag

  @HappyPath @SC-014
  Scenario: A clean cluster reaches Synced and Healthy
    Given a clean minikube cluster
    When I run "make up"
    Then service-a and service-b are Healthy and Synced in ArgoCD

  @HappyPath @SC-015
  Scenario: The example overlay differs from the base
    When I render the "example" overlay
    Then it differs from the base in replicas, log level and timeout
