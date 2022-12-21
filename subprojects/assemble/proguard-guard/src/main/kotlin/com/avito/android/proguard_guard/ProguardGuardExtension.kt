package com.avito.android.proguard_guard

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

public abstract class ProguardGuardExtension {

    internal abstract val variantsConfiguration: NamedDomainObjectContainer<BuildVariantProguardGuardConfiguration>

    @JvmOverloads
    public fun lockVariant(
        variantName: String,
        action: Action<BuildVariantProguardGuardConfiguration> = Action { }
    ) {
        variantsConfiguration.register(variantName).configure {
            action.execute(it)
        }
    }
}
