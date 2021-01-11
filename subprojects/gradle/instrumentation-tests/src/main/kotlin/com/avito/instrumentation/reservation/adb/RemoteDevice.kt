package com.avito.instrumentation.reservation.adb

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner
import org.funktionale.tries.Try

class RemoteDevice(
    private val processRunner: ProcessRunner,
    override val serial: Serial.Remote,
    override val adb: Adb,
    loggerFactory: LoggerFactory
) : Device(loggerFactory) {

    fun disconnect(): Try<String> = processRunner.run(command = "$adb disconnect $serial")

    fun connect(): Try<String> {
        disconnect()

        return processRunner.run("$adb connect $serial")
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
