#!/usr/bin/env bash

# Build and publish documentation to github pages

set -euf -o pipefail

if [ "$(git status -s)" ]
then
    echo "The working directory is dirty. Please commit any pending changes. TODO: fail"
    # TODO: fail
    # exit 1;
fi

cd docs/
docker build --tag android/docs/local .
cd ..

docker run --rm \
        --volume "$(pwd)":/app \
        --volume "$HOME/.ssh":/root/.ssh \
        --volume "${SSH_AUTH_SOCK:-/dev/null}":/tmp/ssh_auth_sock \
        --env "GITHUB_GIT_USER_NAME=${GITHUB_GIT_USER_NAME}" \
        --env "GITHUB_GIT_USER_EMAIL=${GITHUB_GIT_USER_EMAIL}" \
        -w="/app" \
        --entrypoint docs/publish_entrypoint.sh \
        android/docs/local:latest
