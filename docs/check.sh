#!/usr/bin/env bash

set -e

DOCS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "$DOCS_DIR"/../ci/_environment.sh

echo "Lint markdown files..."
docker run --rm \
        --volume "$DOCS_DIR":/docs:ro \
        -w="/docs" \
        ${DOCUMENTATION_IMAGE} \
        mdl content

echo "Dry-run generation..."
docker run --rm \
        --volume "$DOCS_DIR":/docs \
        -w="/docs" \
        ${DOCUMENTATION_IMAGE} \
        sh -c "hugo --renderToMemory --i18n-warnings --minify --theme book"

echo "Everything is OK"
