package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.isAndroidApp
import com.avito.android.lint.slack.LintReportToSlackTaskFactory
import com.avito.impact.configuration.internalModule
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class LintCheck(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var slackChannelForAlerts = ""

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.isAndroidApp()) {
            "Lint check can be applied only to android application modules"
        }

        if (project.buildEnvironment !is BuildEnvironment.CI) return

        if (useImpactAnalysis && !project.internalModule.isModified()) return

        project.pluginManager.withPlugin("com.avito.android.lint-report") {

            val logger = GradleLoggerFactory.fromProject(project).create<LintCheck>()
            val factory = LintReportToSlackTaskFactory(project, logger)

            if (slackChannelForAlerts.isBlank()) {
                // for now it makes sense, but with lint on PR it should be reevaluated
                error("Please provide slackChannelForAlerts for chain $context; lint without reporting is a waste")
            } else {
                val lintSlackReportTaskProvider = factory.registerLintReportToSlackTask(
                    slackChannelId = slackChannelForAlerts
                )
                rootTask.dependsOn(lintSlackReportTaskProvider)
            }
        }
    }
}
