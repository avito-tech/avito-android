package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.instrumentationTask
import com.avito.performance.measurePerformanceTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class PerformanceTestCheck(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var configuration: String = ""

    var enabled = true

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (!enabled) return
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        val checkTask = project.tasks.register<Task>("${context}PerformanceTest") {
            group = "cd"
            description = "Run performance tests"

            require(configuration.isNotBlank()) { "performance configuration should be set" }

            val instrumentationTask = project.tasks.instrumentationTask(configuration)

            instrumentationTask.get().also { task ->
                task.suppressFailure.set(this@PerformanceTestCheck.suppressFailures)
            }

            val measurePerformanceTask = project.tasks.measurePerformanceTask(configuration)

            measurePerformanceTask.get().also { measureTask ->
                measureTask.dependsOn(instrumentationTask)
            }
            dependsOn(measurePerformanceTask)
            dependsOn(instrumentationTask)
        }

        rootTask.dependsOn(checkTask)
    }
}
