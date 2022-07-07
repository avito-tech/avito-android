package com.avito.android.device.avd.internal

import com.avito.android.device.avd.AvdConfig

public class AvdConfigurationProvider(
    private val configurations: Map<ConfigurationKey, AvdConfig>
) {

    public data class ConfigurationKey(val sdk: Int, val type: String)

    internal fun provide(key: ConfigurationKey): AvdConfig {
        return requireNotNull(configurations[key]) {
            "Failed to provide avd config by key $key. Configurations: $configurations"
        }
    }
}
