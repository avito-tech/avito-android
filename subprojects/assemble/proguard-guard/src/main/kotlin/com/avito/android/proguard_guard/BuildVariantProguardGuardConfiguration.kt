package com.avito.android.proguard_guard

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.nio.file.Paths
import kotlin.io.path.pathString

public abstract class BuildVariantProguardGuardConfiguration(
    public val name: String,
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    public val failOnDifference: Property<Boolean> = objects.property<Boolean>().convention(true)

    internal val mergedConfigurationFile: RegularFileProperty = objects.fileProperty()

    public val lockedConfigurationFile: RegularFileProperty = objects.fileProperty().convention(
        layout.projectDirectory.dir("proguard-guard").dir(name).file(
            "locked-configuration.pro"
        )
    )

    public val outputFile: RegularFileProperty = objects.fileProperty().convention(
        layout.buildDirectory.file(
            Paths.get("outputs", "proguard_guard", name, "diff.txt").pathString
        )
    )
}
