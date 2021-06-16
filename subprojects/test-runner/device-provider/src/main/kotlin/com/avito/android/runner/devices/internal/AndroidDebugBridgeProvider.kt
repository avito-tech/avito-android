package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb

internal class AndroidDebugBridgeProvider(
    loggerFactory: LoggerFactory
) {
    private val instance by lazy {
        AndroidDebugBridgeImpl(
            adb = Adb(),
            loggerFactory = loggerFactory
        )
    }

    fun provide(): AndroidDebugBridge = instance
}
