package com.avito.runner.service.worker.device.adb

import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import rx.Single
import java.util.Optional

public class AdbDevicesManager(
    private val commandLine: CommandLineExecutor = CommandLineExecutor.create(),
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
            .toBlocking()
            .value()

    private fun adbDevices(): Single<String> {
        return commandLine.executeProcess(
            command = adb.adbPath,
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
                val shouldRetry = retryCount < ADB_RETRY_COUNT && exception is IllegalStateException
                shouldRetry
            }
            .toSingle()
    }
}

private const val ADB_RETRY_COUNT = 5
