package com.avito.module.dependencies

import com.avito.module.metrics.CollectAppsMetricsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnstableApiUsage")
public class ModuleDependenciesGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.rootProject == target) {
            "must be applied to the root project"
        }
        target.tasks.register("findAndroidApp", FindAndroidAppTask::class.java)
        target.tasks.register("collectAppsMetrics", CollectAppsMetricsTask::class.java) {
            it.outputs.upToDateWhen {
                false // heavy to calculate correct inputs
            }
        }
    }
}
