package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial
import java.io.File

internal interface EmulatorsLogsReporter {

    fun reportEmulatorLogs(emulatorName: Serial, log: String)

    fun redirectLogcat(emulatorName: Serial, device: Device)

    fun getLogFile(podIp: String): File
}
