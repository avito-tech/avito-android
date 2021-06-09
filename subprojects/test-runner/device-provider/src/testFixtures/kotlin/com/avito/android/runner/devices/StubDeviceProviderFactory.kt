package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.StubDevicesProvider
import com.avito.android.runner.devices.model.DeviceType
import com.avito.android.stats.SeriesName
import com.avito.logger.LoggerFactory
import java.io.File

public class StubDeviceProviderFactory(private val loggerFactory: LoggerFactory) : DevicesProviderFactory {

    override fun create(
        deviceType: DeviceType,
        projectName: String,
        configurationName: String,
        tempLogcatDir: File,
        outputDir: File,
        logcatTags: Collection<String>,
        kubernetesNamespace: String,
        runnerPrefix: SeriesName
    ): DevicesProvider {
        return StubDevicesProvider(loggerFactory = loggerFactory)
    }
}
