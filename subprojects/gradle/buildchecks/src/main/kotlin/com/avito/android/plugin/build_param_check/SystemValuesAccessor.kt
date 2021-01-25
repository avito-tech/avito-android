package com.avito.android.plugin.build_param_check

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

@Suppress("UnstableApiUsage")
internal class SystemValuesAccessor(private val providers: ProviderFactory) {

    private val osName: String?
        get() = getSystemProperty("os.name")

    private val javaVersion: String?
        get() = getSystemProperty("java.version")

    private val javaVendor: String?
        get() = getSystemProperty("java.vendor")

    val javaHome: String?
        get() = getEnvironmentVariable("JAVA_HOME")

    // todo it's avito-specific, how to get version generic way?
    val kotlinVersion: String?
        get() = getSystemProperty("kotlinVersion")

    val javaInfo: String
        get() = "$javaVersion (${javaVendor})"

    val isMac: Boolean
        get() = osName?.contains("mac", ignoreCase = true) ?: false

    fun getSystemProperty(name: String): String? {
        return providers.systemProperty(name).getSafe()
    }

    private fun getEnvironmentVariable(name: String): String? {
        return providers.environmentVariable(name).getSafe()
    }

    private fun Provider<String>.getSafe(): String? {
        val result = forUseAtConfigurationTime().orNull
        return if (result.isNullOrBlank()) {
            null
        } else {
            result
        }
    }
}
