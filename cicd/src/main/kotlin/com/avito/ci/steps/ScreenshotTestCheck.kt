package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.instrumentation.instrumentationTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class ScreenshotTestCheck(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var configuration: String = ""

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        val checkTask = project.tasks.register<Task>("${context}ScreenshotTest") {
            group = "ci"
            description = "Run screenshot tests"

            val instrumentationTask = project.tasks.instrumentationTask(configuration)

            instrumentationTask.get().also { task ->
                task.suppressFailure.set(this@ScreenshotTestCheck.suppressFailures)
            }

            dependsOn(instrumentationTask)
        }

        rootTask.dependsOn(checkTask)
    }
}