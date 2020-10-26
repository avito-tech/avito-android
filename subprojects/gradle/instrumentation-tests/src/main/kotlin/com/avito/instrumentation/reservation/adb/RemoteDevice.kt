package com.avito.instrumentation.reservation.adb

import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.runCommand
import org.funktionale.tries.Try

class RemoteDevice(
    override val serial: Serial.Remote,
    override val adb: Adb,
    logger: (String) -> Unit = {}
) : Device(logger) {

    fun disconnect(): Try<String> = runCommand(
        command = "$adb disconnect $serial",
        logger = logger
    )

    fun connect(): Try<String> {
        disconnect()

        return runCommand(
            "$adb connect $serial",
            logger = logger
        )
    }

    override suspend fun waitForBoot() = waitForCommand(
        runner = { connectAndCheck() },
        checker = { it.exists { output -> output == "1" } },
        successMessage = "$serial connected",
        errorMessage = "failed to connect to $serial"
    )

    private fun connectAndCheck(): Try<String> {
        connect()
        return isBootCompleted()
    }
}
