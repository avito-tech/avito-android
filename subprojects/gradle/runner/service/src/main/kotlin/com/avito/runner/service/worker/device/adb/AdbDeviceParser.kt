package com.avito.runner.service.worker.device.adb

class AdbDeviceParser {

    fun parse(output: String): Set<AdbDeviceParams> {
        return output
            .substringAfter("List of devices attached")
            .split(System.lineSeparator())
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
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
