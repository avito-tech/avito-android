package com.avito.runner.service.worker.device.adb

import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.logging.Logger
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DevicesManager

class AdbDevicesManager(
    private val logger: Logger,
    private val commandLine: CommandLineExecutor = CommandLineExecutor.Impl()
) : DevicesManager {
    private val androidHome: String? = System.getenv("ANDROID_HOME")
    private val adb: String = "$androidHome/platform-tools/adb"

    init {
        requireNotNull(androidHome) {
            "Can't find env ANDROID_HOME. It needs to run 'adb'"
        }
    }
    override fun connectedDevices(): Set<Device> =
        commandLine.executeProcess(
            command = adb,
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
                    logger.log("runningEmulators: retrying $exception.")
                }

                shouldRetry
            }
            .map { output ->
                AdbDeviceParser().parse(output)
                    .map { params ->
                        AdbDevice(
                            id = params.id,
                            model = params.model,
                            online = params.online,
                            adb = adb,
                            logger = logger
                        ) as Device
                    }.toSet()
            }
            .doOnError { logger.log("Error on getting adb devices, error = $it") }
            .toSingle()
            .toBlocking()
            .value()
}
