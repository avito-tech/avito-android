#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

source $(dirname $0)/_main.sh

# force Envs be set up
set -uex
function publish() {
    # PGP_KEY is a large 4kb key with unescaped symbols
    local readonly gradle_properties_path="subprojects/gradle.properties"
    if [[ -f ${gradle_properties_path} ]]; then
        echo "avito.pgp.key=$PGP_KEY" >> ${gradle_properties_path}
    else
        echo "${gradle_properties_path} file doesn't exist"
        exit 1;
    fi

    local readonly profileId="-Pavito.ossrh.stagingProfileId=${OSSRH_STAGING_ID}"
    local readonly user="-Pavito.ossrh.user=${OSSRH_USER}"
    local readonly password="-Pavito.ossrh.password=${OSSRH_PASSWORD}"

    local readonly CREDENTIALS="${profileId} ${user} ${password}"
    local readonly PGP="-Pavito.pgp.keyid=${PGP_KEY_ID} -Pavito.pgp.password=${PGP_PASSWORD}"

    runInBuilder "./gradlew -p subprojects publishRelease ${GRADLE_ARGS} ${CREDENTIALS} ${PGP} --no-parallel --stacktrace"
}

publish
