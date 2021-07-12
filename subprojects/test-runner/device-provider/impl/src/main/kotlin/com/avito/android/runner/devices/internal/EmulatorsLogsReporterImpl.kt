package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial
import java.io.File

internal class EmulatorsLogsReporterImpl(
    private val outputFolder: File,
    private val logcatDir: File,
    private val logcatTags: Collection<String>
) : EmulatorsLogsReporter {

    override fun reportEmulatorLogs(emulatorName: Serial, log: String) {
        getLogFile(emulatorName.value)
            .apply { parentFile?.mkdirs() }
            .writeText(log)
    }

    override fun redirectLogcat(emulatorName: Serial, device: Device) {
        val logcatFile = getFile(
            dir = logcatDir,
            emulatorName = emulatorName.value
        )

        device.redirectLogcatToFile(
            file = logcatFile,
            tags = logcatTags
        )
    }

    override fun getLogFile(podIp: String): File {
        return File(File(outputFolder, DEVICES_LOGS), "$podIp.txt")
    }

    private fun getFile(
        dir: File,
        emulatorName: String
    ) = File(dir, "$emulatorName.txt").apply { parentFile?.mkdirs() }
}

private const val DEVICES_LOGS = "devices"
