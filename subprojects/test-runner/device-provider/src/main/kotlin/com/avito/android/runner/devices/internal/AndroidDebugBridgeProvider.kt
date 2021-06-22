package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner

public class AndroidDebugBridgeProvider(
    loggerFactory: LoggerFactory,
    processRunner: ProcessRunner
) {
    private val instance by lazy {
        AndroidDebugBridgeImpl(
            adb = Adb(),
            loggerFactory = loggerFactory,
            processRunner = processRunner
        )
    }

    internal fun provide(): AndroidDebugBridge = instance
}
