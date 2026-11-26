repo : onboarding-app (services/service-a)
Service appelant (caller) du ticket B2-5095 : appelle service-b en HTTP et retourne le résultat combiné.
Exposé en REST.
Endpoint : /api/v1/combined
Point d'entrée : controller REST src/main/java/com/videotron/bsi/service/a/web/CombinedRestController.java
Micro Service

Architecture :
Analyse Fonctionnelle : doc/spec/B2-5095/spec.md
