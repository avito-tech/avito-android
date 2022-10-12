# How to test worker locally without k8s

1. Create config-local.json in the worker module dir
2. Execute `./start_worker.sh` in the project root dir. It will start queue then build and start worker
3. Execute `./gradlew :app:emceeTestDebug` from `samples/emcee`
