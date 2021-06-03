package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner

internal class LocalDevice(
    override val serial: Serial.Local,
    override val adb: Adb,
    loggerFactory: LoggerFactory,
    processRunner: ProcessRunner
) : AbstractDevice(loggerFactory, processRunner) {

    override suspend fun waitForBoot() = waitForCommand(
        runner = { isBootCompleted() }
    )
}
