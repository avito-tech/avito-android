package com.avito.android.signer

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * AGP 7.1 will introduce extensions for build variants,
 * could be useful to express variant specific tokens
 * see http://links.k.avito.ru/M9W
 */
public abstract class SignExtension {

    public abstract val enabled: Property<Boolean>

    public abstract val serviceUrl: Property<String>

    /**
     * http client read and write timeouts
     */
    public abstract val readWriteTimeoutSec: Property<Long>

    /**
     * applicationId to token
     */
    public abstract val apkSignTokens: MapProperty<String, String>

    /**
     * applicationId to token
     */
    public abstract val bundleSignTokens: MapProperty<String, String>
}
