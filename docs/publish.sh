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

# TODO: simulate the same user in image
echo "Fix broken from docker permissions in .git"
sudo chown -R "$(whoami)" .git/
