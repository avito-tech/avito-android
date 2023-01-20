#!/usr/bin/env bash

set -ex

CONFIG=config.json

while getopts c: flag;
do
    case "${flag}" in
        c) CONFIG=${OPTARG};;
        *) echo "flag ${flag} not supported";;
    esac
done

adb start-server

# --add-opens solves the Retrofit and Java 11 issue. See https://github.com/square/retrofit/issues/3341
java --add-opens=java.base/java.lang.invoke=ALL-UNNAMED -jar ./emcee-worker.jar start -c $CONFIG -ll info
