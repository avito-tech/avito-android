package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial
import java.io.File

internal object StubEmulatorsLogsReporter : EmulatorsLogsReporter {

    override fun reportEmulatorLogs(emulatorName: Serial, log: String) {
        // empty
    }

    override fun redirectLogcat(emulatorName: Serial, device: Device) {
        // empty
    }

    override fun getLogFile(podIp: String): File {
        return File.createTempFile("stub-log-$podIp", "txt")
    }
}
