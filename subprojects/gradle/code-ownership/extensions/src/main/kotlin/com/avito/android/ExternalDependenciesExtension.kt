package com.avito.android

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory

public abstract class ExternalDependenciesExtension(
    layout: ProjectLayout,
    objects: ObjectFactory
) {

    public val libsVersionsFile: RegularFileProperty =
        objects.fileProperty().convention(layout.projectDirectory.file("gradle/libs.versions.toml"))
    public val libsOwnersFile: RegularFileProperty =
        objects.fileProperty().convention(layout.projectDirectory.file("gradle/libs.owners.toml"))
}
