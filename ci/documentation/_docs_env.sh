#!/usr/bin/env bash

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
VENV_DIR="$REPO_ROOT/.venv-docs"

if [[ ! -x "$VENV_DIR/bin/python3" ]]; then
    python3 -m venv "$VENV_DIR"
fi
"$VENV_DIR/bin/pip" install -q -r "$REPO_ROOT/docs/requirements.txt"
