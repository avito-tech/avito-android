package com.avito.android.signer

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * AGP 7.1 will introduce extensions for build variants,
 * could be useful to express variant specific tokens
 * see http://links.k.avito.ru/M9W
 */
public abstract class SignExtension @Inject constructor(
    objects: ObjectFactory
) {

    public abstract val enabled: Property<Boolean>

    public abstract val serviceUrl: Property<String>

    /**
     * http client read and write timeouts
     */
    public abstract val readWriteTimeoutSec: Property<Long>

    /**
     * Enables mTLS configuration for calls to the signing service.
     * Requires applying `com.avito.android.tls-configuration` plugin to the root project.
     */
    public val useTls: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * applicationId to token
     */
    public abstract val apkSignTokens: MapProperty<String, String>

    /**
     * applicationId to token
     */
    public abstract val bundleSignTokens: MapProperty<String, String>
}
