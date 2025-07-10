package com.avito.runner.service.worker.device.adb.request

internal abstract class AdbRequest {

    open fun serialize(deviceSerial: String): List<String> = listOf("-s", deviceSerial) + getArguments()

    protected abstract fun getArguments(): List<String>
}
