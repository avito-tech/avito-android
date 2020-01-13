package com.avito.runner.service.worker.device.adb

import com.avito.runner.ProcessNotification
import com.avito.runner.logging.Logger
import com.avito.runner.process
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DevicesManager

class AdbDevicesManager(
    private val logger: Logger
) : DevicesManager {

    private val androidHome: String by lazy { System.getenv("ANDROID_HOME") }
    private val adb: String by lazy { "$androidHome/platform-tools/adb" }

    override fun connectedDevices(): Set<Device> =
        process(listOf(adb, "devices"))
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
                output
                    .substringAfter("List of devices attached")
                    .split(System.lineSeparator())
                    .asSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .filter { it.contains("online") || it.contains("device") }
                    .map {
                        AdbDevice(
                            id = it.substringBefore("\t"),
                            online = when {
                                it.contains("offline", ignoreCase = true) -> false
                                it.contains("device", ignoreCase = true) -> true
                                else -> throw IllegalStateException("Unknown devicesManager output for device: $it")
                            },
                            adb = adb,
                            logger = logger
                        ) as Device
                    }
                    .toSet()
            }
            .doOnError { logger.log("Error during getting connectedAdbDevices, error = $it") }
            .toSingle()
            .toBlocking()
            .value()
}
