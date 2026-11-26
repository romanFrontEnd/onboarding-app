# ADR 0002: Local Kubernetes cluster

- Status: Accepted
- Date: 2026-11-25
- Change: B2-5095

## Context
The ticket asks for a local cluster (kind, k3d or minikube) with the choice justified. The machine is an Apple Silicon Mac.

## Options considered
| Option | Notes |
|---|---|
| minikube, Docker driver | Native arm64, default containerd runtime, `image load` works with every runtime |
| k3d | Lighter and faster, built-in registry. Recommended on pure resource grounds |
| kind | Not evaluated further |

## Decision
minikube with the Docker driver, started with 3 CPUs and 4 GB. This was the Developer's choice over k3d, whose main advantage (the built-in registry) is not needed with `minikube image load`.

## Consequences
- Slower start and more memory than k3d.
- No built-in registry: images are loaded into the node (ADR 0001).
- Docker Desktop must be running.
- The environment is created and removed by `make up` and `make down` (profile `onboarding`).
