package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.DeviceType
import com.avito.android.stats.SeriesName
import java.io.File

public interface DevicesProviderFactory {

    public fun create(
        deviceType: DeviceType,
        projectName: String,
        configurationName: String,
        tempLogcatDir: File,
        outputDir: File,
        logcatTags: Collection<String>,
        kubernetesNamespace: String,
        runnerPrefix: SeriesName
    ): DevicesProvider
}
