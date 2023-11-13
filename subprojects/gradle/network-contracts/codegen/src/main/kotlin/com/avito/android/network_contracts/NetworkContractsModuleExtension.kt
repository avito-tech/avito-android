package com.avito.android.network_contracts

import org.gradle.api.provider.Property

public abstract class NetworkContractsModuleExtension {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val codegenFilePath: Property<String>

    public abstract val version: Property<String>

    internal companion object {
        internal const val NAME = "networkContracts"
    }
}
