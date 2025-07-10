package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbShellRequest

internal class GetPropAdbShellRequest(
    private val key: String,
) : AdbShellRequest() {

    override fun getArguments(): List<String> = listOf(
        "getprop",
        key,
    )
}
