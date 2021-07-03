package com.avito.runner.service.worker.device.adb

import com.avito.runner.service.worker.device.Serial
import com.google.common.net.InetAddresses

public class AdbDeviceParser {

    public fun findDeviceInOrNull(
        serial: Serial,
        output: String
    ): AdbDeviceParams? {
        return adbDevicesLines(output)
            .filter { line -> line.contains("online") || line.contains("device") }
            .firstOrNull { line -> line.contains(serial.value) }?.let { line ->
                createDeviceParams(line)
            }
    }

    public fun parse(output: String): Set<AdbDeviceParams> {
        return adbDevicesLines(output)
            .filter { it.contains("online") || it.contains("device") }
            .map(this::createDeviceParams)
            .toSet()
    }

    private fun createDeviceParams(line: String): AdbDeviceParams {
        return AdbDeviceParams(
            id = createSerial(line.substringBefore(" ")),
            model = line.substringAfter("model:").substringBefore(" device"),
            online = when {
                line.contains("offline", ignoreCase = true) -> false
                line.contains("device", ignoreCase = true) -> true
                else -> throw IllegalStateException("Unknown devicesManager output for device: $line")
            }
        )
    }

    private fun createSerial(value: String): Serial {
        return if (isRemote(value)) {
            Serial.Remote(value)
        } else {
            Serial.Local(value)
        }
    }

    @Suppress("UnstableApiUsage")
    private fun isRemote(serial: String): Boolean {
        return serial.contains(':')
            && InetAddresses.isInetAddress(serial.substringBefore(':'))
    }

    private fun adbDevicesLines(output: String): Sequence<String> {
        val sanitizedOutput = output
            .substringAfter("List of devices attached")
            .split(System.lineSeparator())
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        check(!sanitizedOutput.contains("error: cannot connect to daemon")) {
            "Cannot connect to adb daemon:\n$output"
        }
        return sanitizedOutput
    }
}
