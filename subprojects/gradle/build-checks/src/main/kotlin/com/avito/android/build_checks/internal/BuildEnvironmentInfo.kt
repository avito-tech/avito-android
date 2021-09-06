package com.avito.android.build_checks.internal

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class BuildEnvironmentInfo(private val providers: ProviderFactory) {

    private val osName: String?
        get() = getSystemProperty("os.name")

    private val javaVersion: String?
        get() = getSystemProperty("java.version")

    private val javaVendor: String?
        get() = getSystemProperty("java.vendor")

    val javaHome: String?
        get() = getEnvironmentVariable("JAVA_HOME")

    val javaInfo: String
        get() = "$javaVersion ($javaVendor)"

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
