#!/usr/bin/env bash

set -ex

adb start-server
java -jar ./emcee-worker.jar start -c config.json -ll info
