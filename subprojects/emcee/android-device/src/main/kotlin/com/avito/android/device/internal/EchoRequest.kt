package com.avito.android.device.internal

import com.malinskiy.adam.request.shell.v1.ShellCommandRequest

internal class EchoRequest(value: String) : ShellCommandRequest("echo $value")
