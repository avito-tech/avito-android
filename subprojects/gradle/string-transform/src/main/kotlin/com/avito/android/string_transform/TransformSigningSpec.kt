package com.avito.android.string_transform

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class TransformSigningSpec @Inject constructor(
    objects: ObjectFactory,
) {

    public val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}
