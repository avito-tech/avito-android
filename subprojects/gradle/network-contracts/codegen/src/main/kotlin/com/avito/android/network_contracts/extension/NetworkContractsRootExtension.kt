package com.avito.android.network_contracts.extension

import com.avito.android.network_contracts.http.Timeouts
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsRootExtension(
    objects: ObjectFactory
) {

    /**
     * Determines whether to use TLS when connecting to the service.
     * Default value: true.
     */
    public val useTls: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * Maximum number of retry attempts for network requests when encountering
     * transient errors or service unavailability (HTTP 5xx, connection timeouts, etc.).
     * Default value: 2.
     */
    public val networkRetries: Property<Int> = objects.property<Int>().convention(2)

    /**
     * Optional timeouts for network operations (connection, read, write).
     */
    public abstract val networkTimeouts: Property<Timeouts>

    /**
     * Base URL of the service used for generating clients.
     * Should be specified in the project configuration.
     */
    public abstract val serviceUrl: Property<String>

    /**
     * Name of the environment variable containing the path to the certificate (.crt file).
     * Used when setting up TLS connections.
     */
    public abstract val crtEnvName: Property<String>

    /**
     * Name of the environment variable containing the path to the private key (.key file).
     * Used when setting up TLS connections.
     */
    public abstract val keyEnvName: Property<String>

    public companion object {
        internal const val NAME = "networkContractsRoot"
    }
}
