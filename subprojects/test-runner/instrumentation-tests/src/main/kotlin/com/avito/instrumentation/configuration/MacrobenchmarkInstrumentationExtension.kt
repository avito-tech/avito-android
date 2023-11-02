package com.avito.instrumentation.configuration

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class MacrobenchmarkInstrumentationExtension(
    objects: ObjectFactory
) {
    public val applicationPackageName: Property<String> = objects.property()

    public val applicationBuildDir: DirectoryProperty = objects.directoryProperty()

    public fun isAnyPropertyConfigured(): Boolean {
        return listOf(
            applicationPackageName,
            applicationBuildDir
        ).any { it.isPresent }
    }

    public fun finalizeValues() {
        listOf(
            applicationPackageName,
            applicationBuildDir
        )
            .forEach { it.finalizeValue() }
    }
}
