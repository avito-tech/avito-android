package com.avito.plugin

import com.android.builder.model.BuildType
import org.gradle.api.provider.Property

public abstract class SignExtension {

    internal val apkSignTokens = mutableMapOf<String, String?>()

    internal val bundleSignTokens = mutableMapOf<String, String?>()

    @Deprecated("use url")
    public var host: String? = null

    public abstract val enabled: Property<Boolean>

    public abstract val url: Property<String>

    /**
     * http client read and write timeouts
     */
    public abstract val readWriteTimeoutSec: Property<Long>

    public fun apk(variant: BuildType, token: String?) {
        apkSignTokens[variant.name] = token
    }

    public fun bundle(variant: BuildType, token: String?) {
        bundleSignTokens[variant.name] = token
    }
}
