package com.avito.instrumentation.internal

import com.avito.utils.gradle.EnvArgs
import org.gradle.api.provider.Provider

internal class BuildEnvResolver(private val envArgs: Provider<EnvArgs>) {

    fun getBuildId(): String {
        return envArgs.get().build.id.toString()
    }

    fun getBuildType(): String {
        return envArgs.get().build.type
    }
}
