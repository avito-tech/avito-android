package com.avito.android.module_type.validation

import com.avito.android.module_type.validation.configurations.missings.implementations.MissingImplementationDependencyExtension
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

public abstract class ValidationExtension(
    objects: ObjectFactory
) {

    internal val missingImplementationExtension: MissingImplementationDependencyExtension = objects
        .newInstance(MissingImplementationDependencyExtension::class.java)

    public fun missingImplementations(action: Action<MissingImplementationDependencyExtension>) {
        action.execute(missingImplementationExtension)
    }
}
