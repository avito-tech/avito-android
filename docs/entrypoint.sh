#!/usr/bin/env sh

set -e

# TODO: pass external user to avoid permission issues in mounted volumes

# shellcheck disable=SC2145
echo "Running command: $@"

# TODO: -u ${BUILD_USER}
sudo --set-home --preserve-env "PATH=$PATH" "$@"
