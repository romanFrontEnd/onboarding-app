# ADR 0003: Manifest tooling

- Status: Accepted
- Date: 2026-11-25
- Change: B2-5095

## Context
The ticket requires per-environment configuration separated from the base manifests (Kustomize, Helm or equivalent), with at least one example overlay.

## Decision
Plain Kustomize: one base per service (Deployment and ClusterIP Service) and overlays `local` (deployed) and `example` (2 replicas, DEBUG log level, longer timeout, own namespace). Manifests are rendered by the Confys Local agent from `deploy/local-config.json` and validated with `kubectl kustomize` (built into kubectl, no extra tool).

## Rejected
- Helm: a chart adds templating and a release concept this demo does not need.
- Confy's corporate templates: they assume Argo Rollouts, Istio, Vault and internal registries that do not exist on minikube.

## Consequences
- Generated files carry no hand edits: change the input file and re-render (`render_local.py --check` reports drift).
- The example overlay is rendered and diffed but not deployed.
