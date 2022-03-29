package com.avito.android.device.internal

import com.malinskiy.adam.request.shell.v1.ShellCommandRequest

internal class ClearPackageRequest(
    appPackage: String
) : ShellCommandRequest(
    cmd = "pm clear $appPackage"
)
