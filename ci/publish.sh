#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path (name)

source $(dirname $0)/_main.sh

# force Envs to be set up
set -uex

function publish() {
    # PGP_KEY is a large 4kb key with unescaped symbols
    local readonly gradle_properties_path="gradle.properties"
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

    runInBuilder "./gradlew publishRelease ${GRADLE_ARGS} ${CREDENTIALS} ${PGP} --no-parallel --stacktrace"
}

readonly CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ "$CURRENT_BRANCH" == "develop" ]; then
    echo "ERROR: Releasing from develop branch. It can clash with a release from a release branch. Develop contains the next version. See a release process in docs."
    exit 1
fi

publish
