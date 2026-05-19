#!/usr/bin/env bash

set -euf -o pipefail

source "$(dirname "${BASH_SOURCE[0]}")/_docs_env.sh"

SITE_DIR="${DOCS_SITE_DIR:-/tmp/avito-github-mkdocs}"
if [[ "$SITE_DIR" != /* ]]; then
    SITE_DIR="$REPO_ROOT/$SITE_DIR"
fi

cd "$REPO_ROOT"

"$VENV_DIR/bin/mkdocs" build --clean --strict --config-file "$REPO_ROOT/docs/mkdocs.yml" --site-dir "$SITE_DIR"
