package com.avito.android.network_contracts.extension

import com.avito.android.network_contracts.extension.urls.UrlConfiguration
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.provider.Property

public abstract class NetworkContractsModuleExtension {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val codegenFilePath: Property<String>

    public abstract val packageName: Property<String>

    public abstract val version: Property<String>

    public abstract val urls: ExtensiblePolymorphicDomainObjectContainer<UrlConfiguration>

    public abstract val useTls: Property<Boolean>

    internal companion object {
        internal const val NAME = "networkContracts"
    }
}
