package com.avito.plugin

import com.android.builder.model.BuildType
import org.gradle.api.provider.Property

abstract class SignExtension {

    internal val apkSignTokens = mutableMapOf<String, String?>()

    internal val bundleSignTokens = mutableMapOf<String, String?>()

    @Deprecated("use url")
    var host: String? = null

    abstract val url: Property<String>

    /**
     * http client read and write timeouts
     */
    abstract val readWriteTimeoutSec: Property<Long>

    fun apk(variant: BuildType, token: String?) {
        apkSignTokens[variant.name] = token
    }

    fun bundle(variant: BuildType, token: String?) {
        bundleSignTokens[variant.name] = token
    }
}
