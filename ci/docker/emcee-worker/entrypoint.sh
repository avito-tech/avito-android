#!/usr/bin/env bash

set -ex

adb start-server

# --add-opens solves the Retrofit and Java 11 issue. See https://github.com/square/retrofit/issues/3341
java --add-opens=java.base/java.lang.invoke=ALL-UNNAMED -jar ./emcee-worker.jar start -c config.json -ll info
