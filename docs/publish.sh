#!/usr/bin/env bash

# Build and publish documentation to github pages
#
# This script is used in "Deploy Github documentation" CI configuration
# http://links.k.avito.ru/zV

set -euf -o pipefail

if [ "$(git status -s)" ]
then
    echo "The working directory is dirty. Please commit pending changes" >&2;
    exit 1;
fi

if [[ -z "${SSH_AUTH_SOCK+x}" ]]; then
    echo "SSH_AUTH_SOCK env is unset. Git via SSH will not work properly." >&2;
    exit 1;
fi

if [[ -z "${GITHUB_GIT_USER_NAME+x}" ]]; then
    echo "GITHUB_GIT_USER_NAME env is unset" >&2;
    exit 1;
fi

if [[ -z "${GITHUB_GIT_USER_EMAIL+x}" ]]; then
    echo "GITHUB_GIT_USER_EMAIL env is unset" >&2;
    exit 1;
fi

DOCS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source "$DOCS_DIR"/../ci/_environment.sh

docker run --rm \
        --volume "$DOCS_DIR/..":/app \
        --volume "${SSH_AUTH_SOCK:-/dev/null}":/tmp/ssh_auth_sock \
        --env "SSH_AUTH_SOCK=/tmp/ssh_auth_sock" \
        --env "LOCAL_USER_ID=$(id -u)" \
        --env "GITHUB_GIT_USER_NAME=${GITHUB_GIT_USER_NAME}" \
        --env "GITHUB_GIT_USER_EMAIL=${GITHUB_GIT_USER_EMAIL}" \
        -w="/app" \
        ${DOCUMENTATION_IMAGE} \
        sh -c "docs/_publish_internal.sh"
