# How to test worker locally without k8s

1. Update `config.json` in the `:subprojects:emcee:worker` module dir
2. Export DOCKER_REGISTRY env variable (`export DOCKER_REGISTRY=...`)
3. Execute `./subprojects/emcee/worker/start_worker.sh` in the project root dir. It will start queue then build and start worker
4. Execute `./gradlew :app:emceeTestDebug` from `samples/emcee`
