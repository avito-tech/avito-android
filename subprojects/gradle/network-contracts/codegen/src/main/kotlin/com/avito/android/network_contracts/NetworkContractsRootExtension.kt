package com.avito.android.network_contracts

import com.avito.android.contracts.platform.extension.configurations.network.NetworkConfiguration
import com.avito.android.contracts.platform.extension.configurations.network.Timeouts
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsRootExtension(
    objects: ObjectFactory
) {

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public val useTls: Property<Boolean> = objects.property<Boolean>().convention(true)

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public val networkRetries: Property<Int> = objects.property<Int>().convention(2)

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public abstract val networkTimeouts: Property<Timeouts>

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public abstract val serviceUrl: Property<String>

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public abstract val crtEnvName: Property<String>

    @Deprecated("use network block", replaceWith = ReplaceWith("network {}"))
    public abstract val keyEnvName: Property<String>

    internal val network: NetworkConfiguration = objects.newInstance(NetworkConfiguration::class.java)

    public fun network(configuration: Action<NetworkConfiguration>) {
        configuration.execute(network)
    }

    public companion object {
        internal const val NAME = "networkContractsRoot"
    }
}
