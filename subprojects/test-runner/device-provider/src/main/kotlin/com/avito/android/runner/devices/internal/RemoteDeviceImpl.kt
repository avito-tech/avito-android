package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner
import java.time.Duration

internal class RemoteDeviceImpl(
    override val serial: Serial.Remote,
    override val adb: Adb,
    loggerFactory: LoggerFactory,
    processRunner: ProcessRunner
) : AbstractDevice(loggerFactory, processRunner), RemoteDevice {

    override fun disconnect(): Result<String> =
        processRunner.run(
            command = "$adb disconnect $serial",
            timeout = Duration.ofSeconds(10)
        )

    private suspend fun connect(): Result<String> {
        disconnect()

        return waitForCommand(
            command = {
                processRunner.run(
                    command = "$adb connect $serial",
                    timeout = Duration.ofSeconds(30)
                )
            },
            timeoutSec = 120,
            attempts = 12,
            frequencySec = 10
        )
    }

    override suspend fun waitForBoot(): Result<String> {
        return connect().flatMap {
            isBootCompleted()
                .flatMap { bootResult ->
                    if (bootResult == "1") {
                        Result.Success(bootResult)
                    } else {
                        Result.Failure(RuntimeException("Failed to connect to $serial"))
                    }
                }
        }
    }
}
