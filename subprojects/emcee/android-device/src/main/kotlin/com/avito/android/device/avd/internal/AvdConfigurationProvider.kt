package com.avito.android.device.avd.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.device.avd.AvdConfig

public class AvdConfigurationProvider(
    private val configurations: Map<ConfigurationKey, AvdConfig>
) {

    public data class ConfigurationKey(val sdk: Int, val type: String)

    internal fun provide(key: ConfigurationKey): AvdConfig {
        return configurations[key] ?: throw Problem(
            shortDescription = """
                |There is no AVD configuration declared for the $key.
                |Available configurations: $configurations.
            """.trimMargin(),
            context = "Providing configuration while starting AVD",
            possibleSolutions = listOf("""
                |Declare AVD configuration in the worker configuration file. 
                |Specify it's type, image and sdcard filenames. 
            """.trimMargin())
        ).asRuntimeException()
    }
}
