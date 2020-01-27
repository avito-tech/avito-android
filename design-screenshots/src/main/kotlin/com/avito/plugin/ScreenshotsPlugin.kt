package com.avito.plugin

import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ScreenshotsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { applicationVariant ->
                project.tasks.register<PullScreenshotsTask>("recordScreenshots") {
                    group = "design"
                    description = "Create and pull screenshots from device"
                }
                project.tasks.register<ClearScreenshotsTask>("clearScreenshots") {
                    group = "design"
                    description = "Clear screenshots on device"
                }
            }
        }
    }
}
