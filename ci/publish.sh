#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)
# force Envs be set up
set -u

source $(dirname $0)/_main.sh
local readonly profileId="-Pavito.ossrh.stagingProfileId=${$OSSRH_STAGING_ID}"
local readonly user="-Pavito.ossrh.user=${OSSRH_USER}"
local readonly password="-Pavito.ossrh.password=${OSSRH_PASSWORD}"

local readonly CREDENTIALS = "${profileId} ${user} ${password}"

local readonly PGP = "-Pavito.pgp.keyid=${$PGP_KEY_ID} -Pavito.pgp.key=${PGP_KEY} -Pavito.pgp.password=${PGP_PASSWORD}"

runInBuilder "./gradlew -p subprojects publishRelease ${GRADLE_ARGS} ${CREDENTIALS} ${PGP} --no-parallel --stacktrace"
