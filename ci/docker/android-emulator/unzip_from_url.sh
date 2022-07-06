#!/bin/bash

set -e

TEMP_ZIP_FILE=temp.zip

curl "$1" --progress-bar --location --output $TEMP_ZIP_FILE &&
    unzip $TEMP_ZIP_FILE -d "$2" &&
    rm -f $TEMP_ZIP_FILE
