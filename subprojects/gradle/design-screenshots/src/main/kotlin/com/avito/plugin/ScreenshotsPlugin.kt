package com.avito.plugin

import com.avito.android.withAndroidApp
import com.avito.capitalize
import com.avito.kotlin.dsl.getMandatoryLongProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.time.Duration

public class ScreenshotsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { applicationVariant ->
                if (applicationVariant.name != "release") {
                    val adbPullTimeoutProp =
                        Duration.ofSeconds(project.getMandatoryLongProperty("avito.device.adbPullTimeoutSeconds"))

                    project.tasks.register<PullScreenshotsTask>(
                        "recordScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        description = "Create and pull screenshots from device"

                        applicationIdProperty.set(applicationVariant.testVariant.applicationId)
                        adbPullTimeout.set(adbPullTimeoutProp)
                    }

                    project.tasks.register<ClearScreenshotsTask>(
                        "clearScreenshots${applicationVariant.name.capitalize()}"
                    ) {
                        group = "design"
                        description = "Clear screenshots on device"

                        applicationIdProperty.set(applicationVariant.applicationId)
                        adbPullTimeout.set(adbPullTimeoutProp)
                    }
                }
            }
        }
    }
}
