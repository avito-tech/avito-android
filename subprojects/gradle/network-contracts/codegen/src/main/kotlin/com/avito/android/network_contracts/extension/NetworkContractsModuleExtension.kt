package com.avito.android.network_contracts.extension

import com.avito.android.network_contracts.extension.urls.UrlConfiguration
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class NetworkContractsModuleExtension(
    objects: ObjectFactory
) {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val codegenFilePath: Property<String>

    public abstract val packageName: Property<String>

    public abstract val version: Property<String>

    public abstract val useTls: Property<Boolean>

    internal val urls: ExtensiblePolymorphicDomainObjectContainer<UrlConfiguration> =
        objects.polymorphicDomainObjectContainer(UrlConfiguration::class.java)

    public fun urls(action: Action<PolymorphicDomainObjectContainer<UrlConfiguration>>) {
        action.execute(urls)
    }

    internal companion object {
        internal const val NAME = "networkContracts"
        internal const val SERVICE_URL_NAME = "serviceUrl"
    }
}
