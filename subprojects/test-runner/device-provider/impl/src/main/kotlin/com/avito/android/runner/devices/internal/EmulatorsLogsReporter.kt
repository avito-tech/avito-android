package com.avito.android.runner.devices.internal

import com.avito.k8s.model.KubePod
import com.avito.runner.service.worker.device.Serial
import java.io.File

// todo have two distinct responsibilities, separate it
internal interface EmulatorsLogsReporter {

    fun reportEmulatorLogs(pod: KubePod, emulatorName: Serial, log: String)

    fun redirectLogcat(emulatorName: Serial, device: Device)

    fun getLogFile(podIp: String): File
}
