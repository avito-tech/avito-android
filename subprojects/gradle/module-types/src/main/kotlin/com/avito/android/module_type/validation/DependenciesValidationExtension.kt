package com.avito.android.module_type.validation

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

public abstract class DependenciesValidationExtension(
    objects: ObjectFactory
) {

    internal val publicImplValidationExtension: PublicImplValidationExtension = objects
        .newInstance(PublicImplValidationExtension::class.java)

    public fun publicImpl(action: Action<PublicImplValidationExtension>) {
        action.execute(publicImplValidationExtension)
    }
}
