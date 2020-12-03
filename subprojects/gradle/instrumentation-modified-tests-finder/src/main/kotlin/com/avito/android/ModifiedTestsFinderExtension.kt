package com.avito.android

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class ModifiedTestsFinderExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) {

    @Suppress("UnstableApiUsage")
    internal val output: DirectoryProperty = objects.directoryProperty().convention(layout.buildDirectory)
}
