package com.avito.android.module_type.validation

import com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies.ForbiddenDemoDependenciesExtension
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

public abstract class ValidationExtension(
    objects: ObjectFactory
) {

    internal val forbiddenDemoDependenciesExtension: ForbiddenDemoDependenciesExtension = objects
        .newInstance(ForbiddenDemoDependenciesExtension::class.java)

    public fun forbiddenDemoDependencies(action: Action<ForbiddenDemoDependenciesExtension>) {
        action.execute(forbiddenDemoDependenciesExtension)
    }
}
