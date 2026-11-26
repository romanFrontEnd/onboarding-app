# ADR 0006: Image tagging and promotion

- Status: Accepted
- Date: 2026-11-25
- Change: B2-5095

## Context
AC4: images use immutable tags; a new version is deployed by a Git change that ArgoCD picks up, never by re-pushing a mutable tag.

## Decision
- Tag = short SHA of the last commit that touched `services/<service>`. Deterministic, unique, never reused.
- `scripts/release.sh` order: build, `minikube image load`, confirm the image with `minikube image ls`, pin the tag in `deploy/overlays/local/<service>/kustomization.yaml`, then commit and push. A failed step stops before Git changes (scenario SC-013).
- ArgoCD tracks `main`. Promotion is a Git commit on `main`; the Applications sync automatically (prune and self-heal).
- Deployments use `imagePullPolicy: Never`, so a missing image fails fast (`ErrImageNeverPull`) instead of pulling from a network.
- There is no `latest` tag anywhere. Digest pinning is possible but not enabled.

## Consequences
- Promotion and rollback are ordinary Git operations (commit or revert the tag line).
- Images exist only inside the local cluster, so `make up` rebuilds and reloads them at the pinned tag.
- `release.sh --push` writes straight to `main`, because ArgoCD tracks it. For a team repository, replace this with a pull request.
