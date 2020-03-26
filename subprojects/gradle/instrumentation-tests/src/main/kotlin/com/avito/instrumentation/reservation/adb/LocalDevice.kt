package com.avito.instrumentation.reservation.adb

class LocalDevice(
    serial: String,
    logger: (String) -> Unit = {}
) : Device(serial, logger) {

    override suspend fun waitForBoot() = waitForCommand(
        runner = { isBootCompleted() },
        checker = { it.exists { output -> output == "1" } },
        successMessage = "$serial is booted",
        errorMessage = "failed to boot emulator $serial"
    )
}
