package com.avito.runner.service.worker.device.adb

import com.avito.logger.Logger
import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import rx.Single
import java.util.Optional

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

    override fun findDevice(coordinate: Serial): Optional<Device> {
        return adbDevices().map { output ->
            AdbDeviceParser().findDeviceInOrNull(coordinate, output)?.let { params ->
                Optional.of(
                    AdbDevice(
                        id = params.id,
                        model = params.model,
                        online = params.online,
                        adb = adb,
                        logger = logger
                    ) as Device
                )
            } ?: Optional.empty()
        }
            .toBlocking()
            .value()
    }

    override fun connectedDevices(): Set<Device> =
        adbDevices()
            .map { output ->
                AdbDeviceParser().parse(output)
                    .map { params ->
                        AdbDevice(
                            id = params.id,
                            model = params.model,
                            online = params.online,
                            adb = adb,
                            logger = logger
                        )
                    }.toSet()
            }
            .doOnError {
                logger.warn("Error on getting adb devices", it)
            }
            .toBlocking()
            .value()

    private fun adbDevices(): Single<String> {
        return commandLine.executeProcess(
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
                    logger.debug("runningEmulators: retrying $exception.")
                }

                shouldRetry
            }
            .toSingle()
    }
}
