package com.avito.runner.service.worker.device.adb

import com.avito.logger.Logger
import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import rx.Single
import java.util.Optional

class AdbDevicesManager(
    private val logger: Logger,
    private val commandLine: CommandLineExecutor = CommandLineExecutor.Impl(),
    private val adbParser: AdbDeviceParser = AdbDeviceParser(),
    private val adb: Adb
) : DevicesManager {

    override fun findDevice(coordinate: Serial): Optional<AdbDeviceParams> {
        return adbDevices().map { output ->
            adbParser.findDeviceInOrNull(coordinate, output)?.let { params ->
                Optional.of(
                    params
                )
            } ?: Optional.empty()
        }
            .toBlocking()
            .value()
    }

    override fun connectedDevices(): Set<AdbDeviceParams> =
        adbDevices()
            .map { output ->
                adbParser.parse(output)
            }
            .doOnError {
                logger.warn("Error on getting adb devices", it)
            }
            .toBlocking()
            .value()

    private fun adbDevices(): Single<String> {
        return commandLine.executeProcess(
            command = adb.adb,
            args = listOf("devices", "-l")
        )
            .ofType(ProcessNotification.Exit::class.java)
            .map { it.output }
            .map {
                when (it.contains("List of devices attached")) {
                    true -> it
                    false -> throw IllegalStateException("Adb output is not correct: $it.")
                }
            }
            .retry { retryCount, exception ->
                val shouldRetry = retryCount < 5 && exception is IllegalStateException
                if (shouldRetry) {
                    logger.debug("runningEmulators: retrying $exception.")
                }

                shouldRetry
            }
            .toSingle()
    }
}
