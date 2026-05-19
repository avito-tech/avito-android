#!/usr/bin/env bash

set -euf -o pipefail

source "$(dirname "${BASH_SOURCE[0]}")/_docs_env.sh"

if [[ "$OSTYPE" == linux* ]]; then
    bash -c "sleep 2; xdg-open http://localhost:8000" &
elif [[ "$OSTYPE" == darwin* ]]; then
    bash -c "sleep 2; open http://localhost:8000" &
fi

cd "$REPO_ROOT"

"$VENV_DIR/bin/mkdocs" serve --config-file "$REPO_ROOT/docs/mkdocs.yml"
