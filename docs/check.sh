#!/usr/bin/env bash

set -e

DOCS_DIR=$(dirname "$0")
cd "$DOCS_DIR"

echo "Checking documentation..."
docker build --tag android/docs/local .
docker run --rm \
        --volume "$(pwd)":/app \
        -w="/app" \
        android/docs/local:latest \
        sh -c "hugo --renderToMemory --i18n-warnings --minify --theme book"
