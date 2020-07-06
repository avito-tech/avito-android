#!/usr/bin/env bash

# Build documentation locally

set -euf -o pipefail

DOCS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source "$DOCS_DIR"/../ci/_environment.sh
source "$DOCS_DIR"/check.sh

HOST=http://localhost:1313/avito-android

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

docker run -t -i --rm \
        --volume "${DOCS_DIR}":/docs \
        -w="/docs" \
        -p 1313:1313 \
        ${DOCUMENTATION_IMAGE} \
        sh -c "hugo server --cleanDestinationDir --i18n-warnings --minify $* --bind 0.0.0.0 --baseURL http://localhost:1313/avito-android"
