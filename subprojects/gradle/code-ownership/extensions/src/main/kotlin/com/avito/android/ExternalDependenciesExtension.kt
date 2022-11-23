package com.avito.android

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory

public abstract class ExternalDependenciesExtension(
    layout: ProjectLayout,
    objects: ObjectFactory
) {

    /**
     * Toml file with external dependencies config. It contains all dependencies and their versions.
     *
     * Format is described
     * [here](https://docs.gradle.org/current/userguide/platforms.html#sub:conventional-dependencies-toml).
     *
     * Example:
     * ```toml
     * [libraries]
     * android-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
     * ```

     */
    public val libsVersionsFile: RegularFileProperty =
        objects.fileProperty().convention(layout.projectDirectory.file("gradle/libs.versions.toml"))

    /**
     *
     * Toml file with owners config for external dependencies.
     * Format is similar to format of libs.versions.toml, but instead of dependency you must declare an owner.
     *
     * Example:
     * ```toml
     * [libraries]
     * android-constraintLayout = "Owner name"
     * ```
     */
    public val libsOwnersFile: RegularFileProperty =
        objects.fileProperty().convention(layout.projectDirectory.file("gradle/libs.owners.toml"))
}
