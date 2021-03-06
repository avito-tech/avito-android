package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.isAndroidApp
import com.avito.android.lint.AndroidLintAccessor
import com.avito.android.lint.slack.LintReportToSlackTaskFactory
import com.avito.impact.configuration.internalModule
import com.avito.logger.GradleLoggerFactory
import com.avito.slack.model.SlackChannel
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public class LintCheck(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    // public api
    @Suppress("MemberVisibilityCanBePrivate")
    public var slackChannelForAlerts: SlackChannel? = null

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.isAndroidApp()) {
            "Lint check can be applied only to android application modules"
        }

        if (project.buildEnvironment !is BuildEnvironment.CI) return

        if (useImpactAnalysis && !project.internalModule.isModified()) return

        rootTask.dependsOn(
            AndroidLintAccessor(project).taskProvider()
        )

        project.pluginManager.withPlugin("com.avito.android.lint-report") {

            val logger = GradleLoggerFactory.fromProject(project)
            val factory = LintReportToSlackTaskFactory(project, logger)

            val slackChannel = slackChannelForAlerts
            if (slackChannel == null) {
                // for now it makes sense, but with lint on PR it should be reevaluated
                error("Please provide slackChannelForAlerts for chain $context; lint without reporting is a waste")
            } else {
                val lintSlackReportTaskProvider = factory.registerLintReportToSlackTask(
                    channel = slackChannel
                )
                rootTask.dependsOn(lintSlackReportTaskProvider)
            }
        }
    }
}
