package com.avito.plugin

import com.avito.android.withAndroidApp
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class ScreenshotsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { applicationVariant ->
                if (applicationVariant.name != "release") {

                    project.tasks.register<PullScreenshotsTask>(
                        "recordScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        description = "Create and pull screenshots from device"

                        variant.set(applicationVariant)
                        loggerFactory.set(
                            GradleLoggerFactory.fromProject(
                                project = project,
                                pluginName = "ScreenshotsPlugin",
                                taskName = "PullScreenshotsTask"
                            )
                        )
                    }

                    project.tasks.register<ClearScreenshotsTask>(
                        "clearScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        description = "Clear screenshots on device"

                        variant.set(applicationVariant)
                        loggerFactory.set(
                            GradleLoggerFactory.fromProject(
                                project = project,
                                pluginName = "ScreenshotsPlugin",
                                taskName = "ClearScreenshotsTask"
                            )
                        )
                    }
                }
            }
        }
    }
}
