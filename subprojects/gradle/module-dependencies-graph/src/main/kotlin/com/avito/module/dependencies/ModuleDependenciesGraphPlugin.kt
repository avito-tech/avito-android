package com.avito.module.dependencies

import com.avito.module.metrics.CollectAppsMetricsTask
import com.avito.module.metrics.CollectModuleBetweennessCentralityTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class ModuleDependenciesGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.rootProject == target) {
            "must be applied to the root project"
        }
        target.tasks.register<FindAndroidAppTask>("findAndroidApp")
        target.tasks.register<CollectAppsMetricsTask>("collectAppsMetrics") {

            outputs.upToDateWhen {
                false // heavy to calculate correct inputs
            }
        }
        target.tasks.register<CollectModuleBetweennessCentralityTask>("collectModuleBetweennessCentrality") {
            output.set(
                target.layout.buildDirectory
                    .file("reports/modules-betweenness-centrality/modules-betweenness-centrality.csv")
            )
        }
    }
}
