package com.avito.plugin

import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ScreenshotsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { applicationVariant ->
                if (applicationVariant.name != "release") {
                    project.tasks.register<PullScreenshotsTask>(
                        "recordScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        variant.set(applicationVariant)
                        description = "Create and pull screenshots from device"
                    }
                    project.tasks.register<ClearScreenshotsTask>(
                        "clearScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        variant.set(applicationVariant)
                        description = "Clear screenshots on device"
                    }
                }
            }
        }
    }
}
