.PHONY: up down release forward status

up:
	scripts/up.sh

down:
	scripts/down.sh

# make release SERVICE=service-a [ARGS="--push"]
release:
	scripts/release.sh $(SERVICE) $(ARGS)

forward:
	kubectl -n onboarding port-forward svc/service-a 8080:80

status:
	kubectl -n argocd get applications
	kubectl -n onboarding get pods
