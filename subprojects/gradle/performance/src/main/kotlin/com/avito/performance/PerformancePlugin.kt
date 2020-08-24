package com.avito.performance

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.instrumentationTask
import com.avito.instrumentation.withInstrumentationTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

open class PerformancePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.create<PerformanceExtension>("performance")

        project.withInstrumentationTests { instrumentationConfig ->
            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.MDE }
                .forEach { performanceMdeConfig ->
                    project.tasks.instrumentationTask(performanceMdeConfig.name) { instrumentationTask ->

                        project.tasks.register<PerformanceCollectTask>("collect${performanceMdeConfig.name.capitalize()}") {
                            group = TASK_GROUP
                            description = "Collect performance data nightly"

                            dependsOn(instrumentationTask)
                        }

                        project.tasks.register<SendPerformanceMdeTask>("sendPerformanceMdeTask") {
                            group = TASK_GROUP
                            description = "Report for mde calculations"
                        }
                    }
                }

            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.SIMPLE }
                .forEach { performanceConfig ->

                    project.tasks.instrumentationTask(performanceConfig.name) { instrumentationTask ->

                        val sourceBranchResultsCollector =
                            project.tasks.register<PerformanceCollectTask>("collect${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Collect performance data"

                                dependsOn(instrumentationTask)
                            }

                        project.tasks.register<PerformanceCollectTask>("download${performanceConfig.name.capitalize()}") {
                            group = TASK_GROUP
                            description = "Download performance report from target branch build"

                            //there is no need to depend on tests, it is a hack to postpone fetching
                            dependsOn(sourceBranchResultsCollector)
                        }

                        project.tasks.register<PerformanceCompareTask>("compare${performanceConfig.name.capitalize()}") {
                            group = TASK_GROUP
                            description = "Compare performance reports"
                        }

                        project.tasks.register<Task>(measurePerformanceTaskName(performanceConfig.name)) {
                            this.group = TASK_GROUP
                            this.description = "Measure performance"
                        }
                    }
                }
        }
    }
}

private const val TASK_GROUP = "performance"
