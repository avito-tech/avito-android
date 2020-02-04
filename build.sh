#!/usr/bin/env bash

set -e

source $(dirname $0)/_main.sh "build publishToMavenLocal"

docs/check.sh
