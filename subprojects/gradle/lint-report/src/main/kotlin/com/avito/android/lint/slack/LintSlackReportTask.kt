package com.avito.android.lint.slack

import com.avito.android.lint.LintResultsParser
import com.avito.android.lint.teamcity.TeamcityBuildLinkAccessor
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class LintSlackReportTask : DefaultTask() {

    @get:Input
    abstract val slackReportChannel: Property<String>

    @get:InputFile
    abstract val lintXml: RegularFileProperty

    @get:InputFile
    abstract val lintHtml: RegularFileProperty

    @get:Internal
    abstract val slackClient: Property<SlackClient>

    @TaskAction
    fun doWork() {
        val models = createLintParser().parse(
            projectPath = project.path,
            lintXml = lintXml.get().asFile,
            lintHtml = lintHtml.get().asFile
        )

        val teamcityBuildLinkAccessor = createTeamcityBuildLinkAccessor()

        createLintSlackAlert().report(
            lintReport = models,
            channel = SlackChannel(slackReportChannel.get()),
            buildUrl = teamcityBuildLinkAccessor.getBuildUrl()
        )
    }

    private fun createLintParser(): LintResultsParser = LintResultsParser(
        log = project.ciLogger
    )

    private fun createTeamcityBuildLinkAccessor(): TeamcityBuildLinkAccessor = TeamcityBuildLinkAccessor.Impl(project)

    private fun createLintSlackAlert(): LintSlackReporter = LintSlackReporter.Impl(
        slackClient = slackClient.get(),
        logger = ciLogger
    )
}
