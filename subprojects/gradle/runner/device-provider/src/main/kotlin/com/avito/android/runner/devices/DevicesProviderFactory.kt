package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.DeviceType

public interface DevicesProviderFactory {

    public fun create(
        deviceType: DeviceType,
        configurationName: String,
        logcatTags: Collection<String>,
        kubernetesNamespace: String
    ): DevicesProvider
}
