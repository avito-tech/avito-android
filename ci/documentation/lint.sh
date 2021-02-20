#!/usr/bin/env bash

# TODO: add markdown linter MBS-10625

set -euf -o pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck source=ci/_environment.sh
source "$SCRIPT_DIR"/../_environment.sh

docker run --rm \
    -p 8000:8000 \
    --volume "$SCRIPT_DIR/../..":/app \
    -w="/app" \
    "${DOCUMENTATION_IMAGE}" \
    mkdocs build --clean --strict --config-file docs/mkdocs.yml --site-dir /tmp/avito-github-mkdocs
