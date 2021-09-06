package com.avito.android.lint.slack

import com.avito.android.lint.AndroidLintAccessor
import com.avito.android.lint.LintReportExtension
import com.avito.android.lint.internal.validInGradleTaskName
import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.time.DefaultTimeProvider
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class LintReportToSlackTaskFactory(
    private val project: Project,
    loggerFactory: LoggerFactory,
    private val androidLintAccessor: AndroidLintAccessor = AndroidLintAccessor(project)
) {

    private val logger = loggerFactory.create<LintReportToSlackTaskFactory>()

    private val extension: LintReportExtension by lazy {
        project.extensions.getByType()
    }

    private val slackClientProvider: Provider<SlackClient> by lazy {
        extension.slackToken.zip(extension.slackWorkspace) { token, workspace ->
            SlackClient.create(
                serviceName = "lint-report-slack",
                token = token,
                workspace = workspace,
                httpClientProvider = HttpClientProvider(
                    statsDSender = project.statsd.get(),
                    timeProvider = DefaultTimeProvider(),
                    loggerFactory = loggerFactory
                )
            )
        }
    }

    /**
     * To be used in CiStep, because slack channel only known from there
     */
    public fun registerLintReportToSlackTask(channel: SlackChannel): TaskProvider<LintSlackReportTask> {

        val taskName = "lintReportTo${channel.name.validInGradleTaskName()}"

        var taskProvider = project.tasks.typedNamedOrNull<LintSlackReportTask>(taskName)

        if (taskProvider == null) {
            taskProvider = project.tasks.register<LintSlackReportTask>(taskName) {
                group = "ci"
                description = "Report to slack channel ${channel.name} about lint errors"

                projectPath.set(project.path)

                dependencyOn(androidLintAccessor.taskProvider()) {
                    lintXml.set(androidLintAccessor.resultXml())
                    lintHtml.set(androidLintAccessor.resultHtml())
                }

                slackReportChannel.set(channel)
                slackChannelForLintBugs.set(extension.slackChannelToReportLintBugs)

                slackClient.set(slackClientProvider)

                loggerFactory.set(
                    GradleLoggerFactory.fromTask(
                        project = project,
                        taskName = this.name,
                    )
                )
            }
        } else {
            logger.warn("LintCheck: task $taskName already created in another ciStep; multiple reports are possible")
        }

        return taskProvider
    }
}
