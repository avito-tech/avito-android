package com.avito.runner.service.worker.device.adb.request

internal abstract class AdbShellRequest : AdbRequest() {

    override fun serialize(deviceSerial: String): List<String> = listOf("-s", deviceSerial, "shell") + getArguments()
}
