package com.avito.android.contracts.platform.extension

import com.avito.android.contracts.platform.extension.configurations.fixation.FixationConfiguration
import com.avito.android.contracts.platform.extension.configurations.network.NetworkConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory

public abstract class ContractsRootExtension(objects: ObjectFactory) {

    public val network: NetworkConfiguration = objects.newInstance(NetworkConfiguration::class.java, objects)

    public val fixations: NamedDomainObjectContainer<FixationConfiguration> =
        objects.domainObjectContainer(FixationConfiguration::class.java)

    public companion object {
        public const val NAME: String = "contractsRoot"
    }
}

public fun ContractsRootExtension.network(action: NetworkConfiguration.() -> Unit) {
    action.invoke(network)
}
