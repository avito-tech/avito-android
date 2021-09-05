package com.avito.module.dependencies

import com.avito.logger.GradleLoggerFactory
import com.avito.module.metrics.CollectAppsMetricsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ModuleDependenciesGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.rootProject == target) {
            "must be applied to the root project"
        }
        target.tasks.register("findAndroidApp", FindAndroidAppTask::class.java) {
            it.loggerFactory.set(
                GradleLoggerFactory.fromProject(
                    project = target,
                    pluginName = "ModuleDependenciesGraphPlugin",
                    taskName = "FindAndroidAppTask"
                )
            )
        }
        target.tasks.register("collectAppsMetrics", CollectAppsMetricsTask::class.java) {
            it.loggerFactory.set(
                GradleLoggerFactory.fromProject(
                    project = target,
                    pluginName = "ModuleDependenciesGraphPlugin",
                    taskName = "CollectAppsMetricsTask"
                )
            )

            it.outputs.upToDateWhen {
                false // heavy to calculate correct inputs
            }
        }
    }
}
