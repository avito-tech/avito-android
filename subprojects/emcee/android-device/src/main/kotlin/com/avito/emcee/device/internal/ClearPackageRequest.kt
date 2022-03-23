package com.avito.emcee.device.internal

import com.malinskiy.adam.request.shell.v1.ShellCommandRequest


internal class ClearPackageRequest(
    `package`: String
) : ShellCommandRequest(
    cmd = "pm clear $`package`"
)
