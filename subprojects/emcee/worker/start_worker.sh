set -xeu

# Removes docker container and kills all child processes
trap "trap - SIGTERM && docker rm --force emcee-queue && kill -- -$$" SIGINT SIGTERM EXIT ERR

echo "Starting queue..."
docker run --rm --network="host" --name="emcee-queue" $DOCKER_REGISTRY/android/emcee-queue:ffd97d9356aa >/dev/null 2>&1 &
echo "Queue started"

echo "Building worker..."
./gradlew :subprojects:emcee:worker:fatJar --quiet >/dev/null

echo "Starting worker..."

if [[ "$#" -eq 1 ]]; then
    LOG_LEVEL=$1
else
    LOG_LEVEL=info
fi

# --add-opens solves the Retrofit and Java 11 issue. See https://github.com/square/retrofit/issues/3341
java --add-opens=java.base/java.lang.invoke=ALL-UNNAMED -jar ./subprojects/emcee/worker/build/libs/emcee-worker.jar start -c ./subprojects/emcee/worker/config.json -ll $LOG_LEVEL
