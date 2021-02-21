#!/usr/bin/env bash

# Build and preview documentation locally

set -euf -o pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck source=ci/_environment.sh
source "$SCRIPT_DIR"/../_environment.sh

function openBrowser() {
    local HOST=http://localhost:8000

    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        bash -c "sleep 1; xdg-open ${HOST}" &
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        bash -c "sleep 1; open ${HOST}" &
    else
        echo "Please, fix this script for your OS: ${OSTYPE}"
    fi
}

openBrowser

# NB: Can't use strict mode with serving. Use lint.sh for checking.
docker run --rm -it \
    -p 8000:8000 \
    --volume "$SCRIPT_DIR/../..":/app \
    -w="/app" \
    "${DOCUMENTATION_IMAGE}" \
    mkdocs serve --dev-addr=0.0.0.0:8000 --config-file docs/mkdocs.yml
