package com.avito.android.contracts.platform.extension

import com.avito.android.contracts.platform.extension.configurations.fixation.FixationConfiguration
import com.avito.android.contracts.platform.extension.configurations.network.NetworkConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory

public abstract class ContractsRootExtension(objects: ObjectFactory) {

    public val networks: NamedDomainObjectContainer<NetworkConfiguration> =
        objects.domainObjectContainer(NetworkConfiguration::class.java).apply {
            register(NetworkConfiguration.DEFAULT)
        }

    public val fixations: NamedDomainObjectContainer<FixationConfiguration> =
        objects.domainObjectContainer(FixationConfiguration::class.java)

    public companion object {
        public const val NAME: String = "contractsRoot"
    }
}

@Deprecated("use defaultNetwork", replaceWith = ReplaceWith("defaultNetwork"))
public fun ContractsRootExtension.network(action: NetworkConfiguration.() -> Unit) {
    defaultNetwork(action)
}

public fun ContractsRootExtension.defaultNetwork(action: NetworkConfiguration.() -> Unit) {
    defaultNetwork.action()
}

internal val ContractsRootExtension.defaultNetwork: NetworkConfiguration
    get() = networks.getByName(NetworkConfiguration.DEFAULT)
