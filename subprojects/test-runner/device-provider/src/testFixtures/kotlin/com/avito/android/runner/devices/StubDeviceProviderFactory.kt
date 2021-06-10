package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.StubDevicesProvider
import com.avito.logger.LoggerFactory
import java.io.File

public class StubDeviceProviderFactory(private val loggerFactory: LoggerFactory) : DevicesProviderFactory {

    public override fun create(tempLogcatDir: File): DevicesProvider {
        return StubDevicesProvider(loggerFactory = loggerFactory)
    }
}
