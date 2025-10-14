package com.avito.android.contracts.platform.extension.configurations

import com.avito.android.contracts.platform.extension.configurations.fixation.FixationConfiguration
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import javax.inject.Inject

public abstract class ContractsVariantRootConfiguration @Inject constructor(
    objects: ObjectFactory
) : Named, ExtensionAware {

    public val fixation: FixationConfiguration = objects.newInstance(FixationConfiguration::class.java)
}

public fun ContractsVariantRootConfiguration.fixation(action: FixationConfiguration.() -> Unit) {
    action.invoke(fixation)
}
