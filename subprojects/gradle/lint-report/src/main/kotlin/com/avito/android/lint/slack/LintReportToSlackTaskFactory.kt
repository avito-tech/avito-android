package com.avito.android.lint.slack

import com.avito.android.lint.AndroidLintAccessor
import com.avito.android.lint.LintReportExtension
import com.avito.android.lint.validInGradleTaskName
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.logger.Logger
import com.avito.slack.SlackClient
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class LintReportToSlackTaskFactory(
    private val project: Project,
    private val logger: Logger,
    private val androidLintAccessor: AndroidLintAccessor = AndroidLintAccessor(project)
) {

    @Suppress("UnstableApiUsage")
    private val slackClientProvider: Provider<SlackClient> by lazy {
        val extension = project.extensions.getByType<LintReportExtension>()

        extension.slackToken.zip(extension.slackWorkspace) { token, workspace ->
            SlackClient.Impl(
                token = token,
                workspace = workspace
            ) as SlackClient
        }
    }

    /**
     * To be used in CiStep, because slack channel only known from there
     */
    fun registerLintReportToSlackTask(slackChannel: String): TaskProvider<LintSlackReportTask> {

        val taskName = "lintReportTo${slackChannel.validInGradleTaskName()}"

        var taskProvider = project.tasks.typedNamedOrNull<LintSlackReportTask>(taskName)

        if (taskProvider == null) {
            logger.info("LintCheck: task $taskName already created in another ciStep; multiple reports are possible")

            taskProvider = project.tasks.register<LintSlackReportTask>(taskName) {
                group = "ci"
                description = "Report to slackChannel $slackChannel about lint errors if any"

                dependencyOn(androidLintAccessor.taskProvider()) {
                    lintXml.set(androidLintAccessor.resultXml())
                    lintHtml.set(androidLintAccessor.resultHtml())
                }

                slackReportChannel.set(slackChannel)

                slackClient.set(slackClientProvider)
            }
        }

        return taskProvider
    }
}
