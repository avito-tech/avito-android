#!/usr/bin/env bash

set -ex

readonly SNAPSHOT_ENABLED=true
./adb_redirect.sh
./run_emulator.sh
