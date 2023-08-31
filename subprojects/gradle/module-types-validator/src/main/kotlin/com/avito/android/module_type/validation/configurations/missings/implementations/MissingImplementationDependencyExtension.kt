package com.avito.android.module_type.validation.configurations.missings.implementations

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

public abstract class MissingImplementationDependencyExtension @Inject constructor(
    objects: ObjectFactory
) {

    public val configurationNames: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(setOf("api", "implementation", "androidTestImplementation"))
}
