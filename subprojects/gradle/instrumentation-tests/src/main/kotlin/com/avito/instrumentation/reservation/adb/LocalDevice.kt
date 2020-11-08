package com.avito.instrumentation.reservation.adb

import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb

class LocalDevice(
    override val serial: Serial.Local,
    override val adb: Adb,
    logger: (String) -> Unit = {}
) : Device(logger) {

    override suspend fun waitForBoot() = waitForCommand(
        runner = { isBootCompleted() },
        checker = { it.exists { output -> output == "1" } },
        successMessage = "$serial is booted",
        errorMessage = "failed to boot emulator $serial"
    )
}
