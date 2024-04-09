package com.avito.module.dependencies

import com.avito.module.metrics.CollectAppsMetricsTask
import com.avito.module.metrics.CollectModuleBetweennessCentralityTask
import com.avito.module.metrics.CollectModuleBetweennessCentralityTask.Companion.OUTPUT_BETWEENNESS_CENTRALITY_PATH
import com.avito.module.metrics.CollectModuleBetweennessCentralityTask.Companion.OUTPUT_GRAPH_PATH
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
            moduleGraphOutput.set(target.layout.buildDirectory.file(OUTPUT_GRAPH_PATH))
            betweennessCentralityOutput.set(target.layout.buildDirectory.file(OUTPUT_BETWEENNESS_CENTRALITY_PATH))
        }
    }
}
