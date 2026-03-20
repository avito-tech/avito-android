package com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import java.io.File
import javax.inject.Inject

public abstract class ForbiddenDemoDependenciesExtension @Inject constructor(
    objects: ObjectFactory
) {

    internal val forbiddenDependenciesFile: RegularFileProperty = objects.fileProperty()

    internal val allowedDependencies: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(emptySet())

    public fun forbiddenDependencies(file: File) {
        forbiddenDependenciesFile.set(file)
    }

    public fun allow(path: String) {
        allowedDependencies.add(path)
    }
}
