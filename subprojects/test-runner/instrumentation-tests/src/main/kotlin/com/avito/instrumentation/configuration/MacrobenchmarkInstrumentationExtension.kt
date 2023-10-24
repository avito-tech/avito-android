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

    public val outputDir: DirectoryProperty = objects.directoryProperty()

    private fun properties() = listOf(
        applicationPackageName,
        applicationBuildDir,
        outputDir,
    )

    public fun isAnyPropertyConfigured(): Boolean = properties().any { it.isPresent }

    public fun finalizeValues() {
        properties()
            .forEach { it.finalizeValue() }
    }
}
