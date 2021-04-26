package com.avito.android.lint.slack

import com.avito.android.lint.LintResultsParser
import com.avito.android.lint.internal.slack.LintSlackReporter
import com.avito.android.lint.internal.teamcity.TeamcityBuildLinkAccessor
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class LintSlackReportTask : DefaultTask() {

    @get:Input
    abstract val slackReportChannel: Property<SlackChannel>

    @get:Input
    abstract val slackChannelForLintBugs: Property<SlackChannel>

    @get:InputFile
    abstract val lintXml: RegularFileProperty

    @get:InputFile
    abstract val lintHtml: RegularFileProperty

    @get:Internal
    abstract val slackClient: Property<SlackClient>

    @TaskAction
    fun doWork() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)

        val models = createLintParser(loggerFactory).parse(
            projectPath = project.path,
            lintXml = lintXml.get().asFile,
            lintHtml = lintHtml.get().asFile
        )

        val teamcityBuildLinkAccessor = createTeamcityBuildLinkAccessor()

        createLintSlackAlert(loggerFactory).report(
            lintReport = models,
            channel = slackReportChannel.get(),
            channelForLintBugs = slackChannelForLintBugs.get(),
            buildUrl = teamcityBuildLinkAccessor.getBuildUrl()
        )
    }

    private fun createLintParser(loggerFactory: LoggerFactory): LintResultsParser =
        LintResultsParser(loggerFactory = loggerFactory)

    private fun createTeamcityBuildLinkAccessor(): TeamcityBuildLinkAccessor = TeamcityBuildLinkAccessor.Impl(project)

    private fun createLintSlackAlert(loggerFactory: LoggerFactory): LintSlackReporter = LintSlackReporter.Impl(
        slackClient = slackClient.get(),
        loggerFactory = loggerFactory
    )
}
