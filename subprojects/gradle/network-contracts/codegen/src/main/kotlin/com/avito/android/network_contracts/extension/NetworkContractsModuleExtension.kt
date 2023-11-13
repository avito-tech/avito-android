package com.avito.android.network_contracts.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsModuleExtension(
    objects: ObjectFactory
) {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val packageName: Property<String>

    public val skipValidation: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    internal companion object {
        internal const val NAME = "networkContracts"
    }
}
