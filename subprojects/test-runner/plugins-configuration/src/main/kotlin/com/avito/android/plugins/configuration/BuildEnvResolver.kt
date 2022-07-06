package com.avito.android.plugins.configuration

import com.avito.utils.gradle.EnvArgs
import org.gradle.api.provider.Provider

public class BuildEnvResolver(private val envArgs: Provider<EnvArgs>) {

    public fun getBuildId(): String {
        return envArgs.get().build.id.toString()
    }

    public fun getBuildType(): String {
        return envArgs.get().build.type
    }
}
