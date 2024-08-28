package com.avito.android.tech_budget.detekt

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class DetektConfiguration @Inject constructor(
    objects: ObjectFactory
) {

    public val enabled: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public abstract val configFiles: ConfigurableFileCollection
}
