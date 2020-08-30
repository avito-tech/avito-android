package com.avito.instrumentation.impact

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class InstrumentationTestImpactAnalysisExtension(project: Project) {

    val output: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory)

    val screenMarkerClass = project.objects.property<String>()

    val screenMarkerMetadataField = project.objects.property<String>()

    val unknownRootId = project.objects.property<Int>().also { it.set(-1) }

    var packageFilter = project.objects.property<String>()
}
