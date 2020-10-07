package com.avito.android.build_verdict

import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

@Suppress("UnstableApiUsage")
abstract class BuildVerdictPluginExtension(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val outputDir: Property<Directory> =
        objects.directoryProperty().convention(layout.projectDirectory.dir("outputs/build-verdict"))
}
