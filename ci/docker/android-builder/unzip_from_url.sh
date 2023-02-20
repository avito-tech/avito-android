#!/bin/bash

set -e

# Why do we use a file instead of pipe?
# - unzip doesn't support reading from pipe
# - jar does, but it doesn't validate input zip.
#   In case of missing or invalid file it won't fail.
TEMP_ZIP_FILE=$(mktemp)

curl "$1" --progress-bar --location --output "$TEMP_ZIP_FILE" &&
    unzip "$TEMP_ZIP_FILE" -d "$2" &&
    rm -f "$TEMP_ZIP_FILE"
