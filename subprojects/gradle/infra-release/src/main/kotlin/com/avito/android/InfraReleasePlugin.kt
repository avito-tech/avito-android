package com.avito.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class InfraReleasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<InfraReleaseExtension>("infraRelease")

        target.tasks.register<ReleaseTask>("infraRelease") {
            group = "cd"

            releaseTag.set(extension.releaseTag)
            previousReleaseTag.set(extension.previousReleaseTag)
        }
    }
}
