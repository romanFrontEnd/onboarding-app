# ADR 0004: Repository layout

- Status: Accepted
- Date: 2026-11-25
- Change: B2-5095

## Context
The ticket leaves open mono-repo or separate repositories and asks to separate application code from deployment configuration.

## Decision
One repository (mono-repo) with a hard folder split: `services/` for code, `deploy/` for configuration, `scripts/` and `Makefile` for the local environment, `doc/` for the SDLC artifacts, `docs/adr/` for decisions. ArgoCD reads only `deploy/`.

## Consequences
- One place to clone, review and reproduce.
- Differs from the BSI production convention of a separate `tibsims.<service>.config` repository. A service promoted to production would split `deploy/` out.
- A code commit and its deployment change live in the same history, so the deployed tag can be traced to the exact source commit.
