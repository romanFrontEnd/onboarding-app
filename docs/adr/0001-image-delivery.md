# ADR 0001: Image delivery to the local cluster

- Status: Accepted (spec B2-5095 v0.6)
- Date: 2026-11-25
- Change: B2-5095

## Context
Both services run on a local minikube cluster (Docker driver, arm64, containerd) and are deployed by ArgoCD from GitHub. The ticket requires immutable image tags (AC4), images built and stored locally, and no manual `kubectl apply` in the deployment path.

## Options considered
| Option | Verdict |
|---|---|
| `minikube image load` with git-SHA tags | Chosen. Works with every runtime, simple, verified by spike. |
| Registry addon with a `socat` container | Rejected. On macOS it needs a `socat` container kept running on the Docker host (minikube registry handbook). Fragile. |
| `docker-env` (in-cluster Docker daemon) | Rejected. Needs `--container-runtime=docker` and must be redone after every restart on the Docker driver. |
| Host-run registry | Rejected for now. Needs insecure-registry configuration at cluster start. Not verified. |
| GHCR (GitHub Container Registry) | Rejected. The ticket says images are stored locally, and the current token lacks `write:packages`. |

## Decision
Build images tagged with the git short SHA, load them with `minikube image load`, and deploy with `imagePullPolicy: Never`.
`scripts/release.sh` enforces the order: build, load, confirm with `minikube image ls`, bump the tag in `deploy/overlays/<env>`, commit, push. A tag is never committed before its image is confirmed in the cluster.

## Evidence (spike, 2026-11-25, minikube 1.39.0)
- Applying a Deployment before the image is loaded fails fast with `ErrImageNeverPull`.
- After `minikube image load` the Deployment rolls out and becomes Ready.
- Changing only the manifest tag to a second loaded SHA rolls out the new version.
- ArgoCD syncs an Application from a public GitHub repo to `Synced/Healthy` on this cluster.

## Consequences
- Images exist only inside the local cluster. A second engineer rebuilds and loads them through the bootstrap script (AC9).
- The end-to-end tag-bump flow on the real repo is confirmed on the first build.
- A later move to a real registry changes the delivery step and the image names, not the GitOps structure.
