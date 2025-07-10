package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbShellRequest

internal class ClearPackageAdbShellRequest(
    private val packageName: String,
) : AdbShellRequest() {

    override fun getArguments(): List<String> = listOf(
        "pm",
        "clear",
        packageName,
    )
}
