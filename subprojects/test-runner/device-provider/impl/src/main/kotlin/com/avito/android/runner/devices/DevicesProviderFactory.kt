package com.avito.android.runner.devices

import com.avito.runner.service.DeviceWorkerPoolProvider
import java.io.File

public interface DevicesProviderFactory {
    public fun create(
        tempLogcatDir: File,
        deviceWorkerPoolProvider: DeviceWorkerPoolProvider
    ): DevicesProvider
}
