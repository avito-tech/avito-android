package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.kotlin.dsl.typedNamedOrNull
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class UiTestCheck(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var configurations = mutableListOf<String>()

    /**
     * нам пока нужна отправка только по запуску на всех устройствах
     */
    var sendStatistics: Boolean = false

    fun configurations(vararg configs: String) {
        configurations.addAll(configs)
    }

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        val checkTask = project.tasks.register<Task>("${context}InstrumentationTest") {
            group = "cd"
            description = "Run all instrumentation tests needed for release"

            dependsOn("preInstrumentation") // TODO: hide this task in instrumentation plugin

            configurations.forEach { configuration ->
                val taskName = "instrumentation${configuration.capitalize()}"

                val uiTestTask =
                    requireNotNull(project.tasks.typedNamedOrNull<InstrumentationTestsTask>(taskName)) {
                        "Cannot find task with name $taskName in project"
                    }

                // it is safe to call get() here because task instrumentationXXX must be ready here
                // TODO: can we do it in configure block anyway?
                uiTestTask.get().also { task ->
                    task.suppressFailure.set(this@UiTestCheck.suppressFailures)
                    task.sendStatistics.set(this@UiTestCheck.sendStatistics)

                    // TODO: how to switch off impact analysis?
                    // this.useImpactAnalysis.set(this@UiTestCheck.useImpactAnalysis)
                }

                dependsOn(uiTestTask)
            }
        }

        rootTask.dependsOn(checkTask)
    }
}
