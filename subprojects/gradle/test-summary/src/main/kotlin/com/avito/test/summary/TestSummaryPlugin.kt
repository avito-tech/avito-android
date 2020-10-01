package com.avito.test.summary

import com.avito.bitbucket.Bitbucket
import com.avito.slack.ConjunctionMessageUpdateCondition
import com.avito.slack.SameAuthorUpdateCondition
import com.avito.slack.SlackClient
import com.avito.slack.SlackConditionalSender
import com.avito.slack.SlackMessageUpdaterDirectlyToThread
import com.avito.slack.TodayMessageCondition
import com.avito.slack.model.SlackChannel
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<TestSummaryExtension>("testSummary")

        val slackClient: Provider<SlackClient>

        target.tasks.register<TestSummaryTask>("testSummary") {
            reportCoordinates.set() //todo from instrumentation task
            slackToken.set(extension.slackToken)
            slackWorkspace.set(extension.slackWorkspace)
            reportsHost.set(extension.reportsHost)
            summaryChannel.set(extension.summaryChannel)
            buildUrl.set(extension.buildUrl)
            reportViewerUrl.set(extension.reportViewerUrl)
            unitToChannelMapping.set(extension.unitToChannelMapping)
            mentionOnFailures.set(extension.mentionOnFailures)
            reserveSlackChannel.set(extension.reserveSlackChannel)
            slackUserName.set(extension.slackUserName)
        }

        target.tasks.register<FlakyReportTask>("flakyReport") {

        }
    }
}

abstract class FlakyReportTask : DefaultTask() {

    @TaskAction
    fun doWork() {

    }

    private fun createFlakyTestReporter(
        summaryChannel: SlackChannel,
        slackUsername: String = "Flaky Test Detector"
    ): FlakyTestReporterImpl {
        return FlakyTestReporterImpl(
            slackClient = SlackConditionalSender(
                slackClient = slackClient,
                updater = SlackMessageUpdaterDirectlyToThread(slackClient, logger),
                condition = ConjunctionMessageUpdateCondition(
                    listOf(
                        SameAuthorUpdateCondition(slackUsername),
                        TodayMessageCondition(DefaultTimeProvider())
                    )
                ),
                logger = logger
            ),
            summaryChannel = summaryChannel,
            messageAuthor = slackUsername,
            bitbucket = Bitbucket.create(
                bitbucketConfig = params.bitbucketConfig,
                logger = logger,
                pullRequestId = params.pullRequestId
            ),
            sourceCommitHash = params.sourceCommitHash,
            reportViewer = reportViewer,
            logger = logger,
            buildUrl = params.buildUrl,
            currentBranch = params.currentBranch,
            reportCoordinates = params.reportCoordinates
        )
    }
}
