#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path (name)

set -e

source $(dirname "$0")/_main.sh

bash $(dirname "$0")/documentation/lint.sh

if [[ -z "${PR_INSTRUMENTATION_CACHE_ENABLED+x}" || -z $PR_INSTRUMENTATION_CACHE_ENABLED ]]; then
    echo "PR_INSTRUMENTATION_CACHE_ENABLED env is unset" >&2;
    exit 1;
fi

GRADLE_ARGS+="-Pavito.instrumentation.buildCacheEnabled=${PR_INSTRUMENTATION_CACHE_ENABLED} "

runInBuilder "set -e; ./gradlew build ${GRADLE_ARGS} --stacktrace"
