set -xeu

cleanup() {
    # kill all processes whose parent is this process
    pkill -P $$
}

for sig in INT QUIT HUP TERM; do
  trap "
    cleanup
    trap - $sig EXIT
    kill -s $sig "'"$$"' "$sig"
done
trap cleanup EXIT

echo "Start queue"
/Users/Shared/projects/ios-test-runner/.build/debug/Emcee startLocalQueueServer --queue-server-configuration-location /Users/Shared/emcee.config --hostname localhost > /dev/null &

echo "Build worker"
./gradlew :subprojects:emcee:worker:build --quiet > /dev//null

echo "Start worker"
java -jar ./subprojects/emcee/worker/build/libs/emcee-worker.jar start -c ./subprojects/emcee/worker/config-local.json -d
