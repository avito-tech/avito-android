#!/usr/bin/env bash

# Script for redirecting all traffic for adb connection

# Detect ip and forward ADB ports outside to outside interface
# These ports have to be exposed in docker
ip=$(ifconfig eth0 | grep 'inet' | cut -d: -f2 | awk '{ print $2}')
socat tcp-listen:5037,bind=$ip,fork tcp:127.0.0.1:5037 &
socat tcp-listen:5554,bind=$ip,fork tcp:127.0.0.1:5554 &
socat tcp-listen:5555,bind=$ip,fork tcp:127.0.0.1:5555 &
