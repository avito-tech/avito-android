package com.avito.runner.service.worker.device.adb

class AdbDeviceParser {

    fun parse(output: String): Set<AdbDeviceParams> {
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
            .filter { it.contains("online") || it.contains("device") }
            .map {
                AdbDeviceParams(
                    id = it.substringBefore(" "),
                    model = it.substringAfter("model:").substringBefore(" device"),
                    online = when {
                        it.contains("offline", ignoreCase = true) -> false
                        it.contains("device", ignoreCase = true) -> true
                        else -> throw IllegalStateException("Unknown devicesManager output for device: $it")
                    }
                )
            }
            .toSet()
    }
}
