package com.avito.instrumentation.reservation.adb

import java.io.File

class EmulatorsLogsReporter(
    private val outputFolder: File,
    private val logcatDir: File,
    private val logcatTags: Collection<String>
) {

    fun reportEmulatorLogs(emulatorName: String, log: String) {
        getFile(
            dir = File(outputFolder, DEVICES_LOGS),
            emulatorName = emulatorName
        ).writeText(log)
    }

    fun redirectLogcat(emulatorName: String, device: Device) {
        val logcatFile = getFile(
            dir = logcatDir,
            emulatorName = emulatorName
        )

        device.redirectLogcatToFile(
            file = logcatFile,
            tags = logcatTags
        )
    }

    private fun getFile(
        dir: File,
        emulatorName: String
    ) = File(dir, "$emulatorName.txt").apply { parentFile?.mkdirs() }
}

private const val DEVICES_LOGS = "devices"
