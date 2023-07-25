package com.avito.android.module_type.validation

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

public open class PublicImplValidationExtension @Inject constructor(
    objects: ObjectFactory
) {

    public val configurationNames: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(setOf("api", "implementation", "androidTestImplementation"))
}
