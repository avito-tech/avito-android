package com.avito.android.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public open class FeatureTogglesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<FeatureTogglesReportExtension>("featureTogglesReport")

        project.tasks.register<FeatureTogglesReportTask>("generateTogglesReport") {
            group = "feature-toggles"
            description = "Generates Feature Toggles Report"

            slackHook.set(extension.slackHook)

            developersToTeam.set(extension.developersToTeam)

            projectDir.set(project.projectDir.path)

            dependsOn("${project.path}:testReleaseUnitTest")
        }
    }
}
