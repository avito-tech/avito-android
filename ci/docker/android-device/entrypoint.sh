#!/usr/bin/env bash

set -eux

# Forward adbd port from device to host
adb forward tcp:5555 tcp:5555

# Detect ip and forward ADB ports outside to outside interface
ip=$(ifconfig eth0 | grep 'inet addr' | cut -d: -f2 | awk '{ print $1}')
socat tcp-listen:5555,bind=${ip},fork tcp:127.0.0.1:5555 &

# Sleep forever
tail -f /dev/null
