package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial

internal interface EmulatorsLogsReporter {
    fun reportEmulatorLogs(emulatorName: Serial, log: String)
    fun redirectLogcat(emulatorName: Serial, device: Device)
}
