package com.avito.android.network_contracts.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsRootExtension(
    objects: ObjectFactory
) {

    public val useTls: Property<Boolean> = objects.property<Boolean>().convention(true)

    public abstract val serviceUrl: Property<String>

    public abstract val crtEnvName: Property<String>

    public abstract val keyEnvName: Property<String>

    public companion object {
        internal const val NAME = "networkContractsRoot"
    }
}
