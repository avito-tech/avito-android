package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb

internal class LocalDevice(
    override val serial: Serial.Local,
    override val adb: Adb,
    loggerFactory: LoggerFactory
) : Device(loggerFactory) {

    override suspend fun waitForBoot() = waitForCommand(
        runner = { isBootCompleted() },
        checker = { it.exists { output -> output == "1" } },
        successMessage = "$serial is booted",
        errorMessage = "failed to boot emulator $serial"
    )
}
