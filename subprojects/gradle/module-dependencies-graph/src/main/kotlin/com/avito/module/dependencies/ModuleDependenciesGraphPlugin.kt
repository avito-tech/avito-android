package com.avito.module.dependencies

import com.avito.logger.GradleLoggerFactory
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
                GradleLoggerFactory.fromTask(
                    project = target,
                    task = this,
                    plugin = this@ModuleDependenciesGraphPlugin
                )
            )
        }
        target.tasks.register<CollectAppsMetricsTask>("collectAppsMetrics") {
            loggerFactory.set(
                GradleLoggerFactory.fromTask(
                    project = target,
                    task = this,
                    plugin = this@ModuleDependenciesGraphPlugin
                )
            )

            outputs.upToDateWhen {
                false // heavy to calculate correct inputs
            }
        }
    }
}
