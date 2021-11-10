package com.avito.module.dependencies

import com.avito.logger.GradleLoggerPlugin
import com.avito.module.metrics.CollectAppsMetricsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class ModuleDependenciesGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.rootProject == target) {
            "must be applied to the root project"
        }
        target.tasks.register<FindAndroidAppTask>("findAndroidApp") {
            loggerFactory.set(
                GradleLoggerPlugin.getLoggerFactory(this)
            )
        }
        target.tasks.register<CollectAppsMetricsTask>("collectAppsMetrics") {
            loggerFactory.set(
                GradleLoggerPlugin.getLoggerFactory(this)
            )

            outputs.upToDateWhen {
                false // heavy to calculate correct inputs
            }
        }
    }
}
