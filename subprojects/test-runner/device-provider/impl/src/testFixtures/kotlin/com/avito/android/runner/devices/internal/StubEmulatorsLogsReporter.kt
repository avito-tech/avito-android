package com.avito.android.runner.devices.internal

import com.avito.k8s.model.KubePod
import com.avito.runner.service.worker.device.Serial
import java.io.File

internal object StubEmulatorsLogsReporter : EmulatorsLogsReporter {

    override fun reportEmulatorLogs(pod: KubePod, emulatorName: Serial, log: String) {
        // empty
    }

    override fun redirectLogcat(emulatorName: Serial, device: Device) {
        // empty
    }

    override fun getLogFile(podIp: String): File {
        return File.createTempFile("stub-log-$podIp", "txt")
    }
}
