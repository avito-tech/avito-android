package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.preInstrumentationTask
import com.avito.kotlin.dsl.typedNamedOrNull
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class UiTestCheck(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl(),
    FlakyAwareBuildStep by FlakyAwareBuildStep.Impl() {

    var configurations = mutableListOf<String>()

    @Deprecated("remove after 2020.23")
    var sendStatistics: Boolean = false

    fun configurations(vararg configs: String) {
        configurations.addAll(configs)
    }

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        // see LintWorkerApiWorkaround.md
        val preInstrumentationTask = project.tasks.preInstrumentationTask()

        configurations.forEach { configuration ->
            preInstrumentationTask.configure {
                it.logger.debug("setting dependency between preInstrumentation tasks of configuration $configuration")
                it.dependsOn(project.tasks.preInstrumentationTask(configuration))
            }
        }

        val checkTask = project.tasks.register<Task>("${context}InstrumentationTest") {
            group = "cd"
            description = "Run all instrumentation tests needed for $context"

            configurations.forEach { configuration ->
                val taskName = "instrumentation${configuration.capitalize()}"

                val uiTestTask =
                    requireNotNull(project.tasks.typedNamedOrNull<InstrumentationTestsTask>(taskName)) {
                        "Cannot find task with name $taskName in project"
                    }

                // it is safe to call get() here because task instrumentationXXX must be ready here
                // TODO: can we do it in "configure" block anyway?
                uiTestTask.get().also { task ->
                    task.suppressFailure.set(this@UiTestCheck.suppressFailures)
                    task.suppressFlaky.set(this@UiTestCheck.suppressFlaky)

                    // TODO: how to switch off impact analysis?
                    // this.useImpactAnalysis.set(this@UiTestCheck.useImpactAnalysis)
                }

                dependsOn(uiTestTask)
                dependsOn(preInstrumentationTask)
            }
        }

        rootTask.dependsOn(checkTask)
    }
}
