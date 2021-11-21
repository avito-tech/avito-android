package com.avito.plugin

import com.android.builder.model.BuildType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class SignExtension @Inject constructor(private val objects: ObjectFactory) {

    internal val apkSignTokens = mutableMapOf<String, Provider<String>>()

    internal val bundleSignTokens = mutableMapOf<String, Provider<String>>()

    public abstract val enabled: Property<Boolean>

    public abstract val url: Property<String>

    /**
     * http client read and write timeouts
     */
    public abstract val readWriteTimeoutSec: Property<Long>

    @Deprecated("use fun with variant name", ReplaceWith("apk(variant, token)"))
    public fun apk(variant: BuildType, token: String?) {
        apkSignTokens[variant.name] = objects.property<String>().value(token)
    }

    @Deprecated("use fun with variant name", ReplaceWith("bundle(variant, token)"))
    public fun bundle(variant: BuildType, token: String?) {
        bundleSignTokens[variant.name] = objects.property<String>().value(token)
    }

    // AGP 7.1 will introduce extensions for build variants
    // see http://links.k.avito.ru/M9W

    public fun apk(variant: String, token: Property<String>) {
        apkSignTokens[variant] = token
    }

    public fun bundle(variant: String, token: Property<String>) {
        bundleSignTokens[variant] = token
    }

    public fun apk(variant: String, token: String?) {
        apkSignTokens[variant] = objects.property<String>().value(token)
    }

    public fun bundle(variant: String, token: String?) {
        bundleSignTokens[variant] = objects.property<String>().value(token)
    }
}
