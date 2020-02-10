#!/usr/bin/env bash

set -e

TEMP_PROJECT_VERSION="temp-version"

source $(dirname $0)/_main.sh "build publishToMavenLocal -PprojectVersion=${TEMP_PROJECT_VERSION}"
source $(dirname $0)/_main.sh ":subprojects:android-test:instrumentationUi -PinfraVersion=${TEMP_PROJECT_VERSION}"

# todo push new infraVersion on release

docs/check.sh
