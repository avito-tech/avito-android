package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner

internal class RemoteDeviceImpl(
    private val processRunner: ProcessRunner,
    override val serial: Serial.Remote,
    override val adb: Adb,
    loggerFactory: LoggerFactory
) : AbstractDevice(loggerFactory), RemoteDevice {

    override fun disconnect(): Result<String> = processRunner.run(command = "$adb disconnect $serial")

    override fun connect(): Result<String> {
        disconnect()

        return processRunner.run("$adb connect $serial")
    }

    override suspend fun waitForBoot() = waitForCommand(
        runner = {
            connectAndCheck().flatMap { output ->
                if (output == "1") {
                    Result.Success(output)
                } else {
                    Result.Failure(RuntimeException("Failed to connect to $serial"))
                }
            }
        }
    )

    private fun connectAndCheck(): Result<String> {
        connect()
        return isBootCompleted()
    }
}
