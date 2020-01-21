#!/usr/bin/env bash

# Build documentation locally

set -euf -o pipefail

cd wiki/

docker build --tag android/wiki/local .

HOST=http://localhost:1313/

function openBrowser {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        bash -c "sleep 3 ; xdg-open ${HOST}" &
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        bash -c "sleep 3 ; open ${HOST}" &
    else
        echo "Please, fix this script for your OS: ${OSTYPE}"
    fi
}

openBrowser

# WARNING:
# Override by volumes only directories without generated files.
# Otherwise it'll crash hugo due to missing files.
# You can find them in .dockerignore

docker run --rm \
        --volume "$(pwd)"/content:/app/content \
        --volume "$(pwd)"/layouts:/app/layouts \
        --volume "$(pwd)"/themes:/app/themes \
        -p 1313:1313 \
        --entrypoint hugo \
        android/wiki/local:latest \
        server --minify --theme book --bind 0.0.0.0

