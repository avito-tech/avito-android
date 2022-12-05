package com.avito.android.proguard_guard

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.io.File

public abstract class ProguardGuardExtension {

    internal abstract val variantsConfiguration: NamedDomainObjectContainer<BuildVariantProguardGuardConfiguration>

    /**
     * To get the [mergedConfigurationFile] you should add `-printconfiguration [filename]` to your
     * proguard configuration file (https://www.guardsquare.com/manual/configuration/usage).
     */
    @JvmOverloads
    public fun lockVariant(
        variantName: String,
        mergedConfigurationFile: File,
        action: Action<BuildVariantProguardGuardConfiguration> = Action { }
    ) {
        variantsConfiguration.register(variantName).configure {
            it.mergedConfigurationFile.set(mergedConfigurationFile)
            action.execute(it)
        }
    }
}
