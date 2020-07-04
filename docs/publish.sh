#!/usr/bin/env bash

# Build and publish documentation to github pages
# Prerequisites:
# - Valid ssh key to access: https://help.github.com/en/github/authenticating-to-github/connecting-to-github-with-ssh
# - export GITHUB_GIT_USER_NAME
# - export GITHUB_GIT_USER_EMAIL

set -euf -o pipefail

if [ "$(git status -s)" ]
then
    echo "The working directory is dirty. Please commit pending changes"
    exit 1;
fi

DOCS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source "$DOCS_DIR"/../ci/_environment.sh

docker run --rm \
        --volume "$DOCS_DIR/..":/app \
        --volume "$HOME/.ssh":/home/user/.ssh \
        --volume "${SSH_AUTH_SOCK:-/dev/null}":/tmp/ssh_auth_sock \
        --env "LOCAL_USER_ID=$(id -u)" \
        --env "GITHUB_GIT_USER_NAME=${GITHUB_GIT_USER_NAME}" \
        --env "GITHUB_GIT_USER_EMAIL=${GITHUB_GIT_USER_EMAIL}" \
        -w="/app" \
        ${DOCUMENTATION_IMAGE} \
        sh -c "docs/_publish_internal.sh"
