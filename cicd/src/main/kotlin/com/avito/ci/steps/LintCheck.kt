package com.avito.ci.steps

import com.avito.android.isAndroidApp
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.invoke
import com.avito.android.lint.LintReportTask
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment

class LintCheck(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.isAndroidApp()) {
            "Lint check can be applied only to android application modules"
        }
        if (project.buildEnvironment !is BuildEnvironment.CI) return

        project.pluginManager.withPlugin("com.avito.android.lint-report") {

            val lintReports = project.tasks.withType<LintReportTask>()
            lintReports.configureEach {
                it.abortOnError.set(!suppressFailures)
            }
            lintReports.forEach { task ->
                task.onlyIf { !useImpactAnalysis || project.internalModule.lintConfiguration.isModified }
                rootTask {
                    dependsOn(task)
                }
            }
        }
    }
}
