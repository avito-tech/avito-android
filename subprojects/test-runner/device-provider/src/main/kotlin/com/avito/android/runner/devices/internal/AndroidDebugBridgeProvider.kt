package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb

public class AndroidDebugBridgeProvider(
    loggerFactory: LoggerFactory
) {
    private val instance by lazy {
        AndroidDebugBridgeImpl(
            adb = Adb(),
            loggerFactory = loggerFactory
        )
    }

    internal fun provide(): AndroidDebugBridge = instance
}
