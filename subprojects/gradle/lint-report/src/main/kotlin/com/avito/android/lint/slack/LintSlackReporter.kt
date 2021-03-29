package com.avito.android.lint.slack

import com.avito.android.lint.model.LintIssue
import com.avito.android.lint.model.LintIssue.Severity.ERROR
import com.avito.android.lint.model.LintIssue.Severity.INFORMATION
import com.avito.android.lint.model.LintIssue.Severity.UNKNOWN
import com.avito.android.lint.model.LintIssue.Severity.WARNING
import com.avito.android.lint.model.LintReportModel
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannelId
import okhttp3.HttpUrl
import java.io.File

interface LintSlackReporter {

    fun report(
        lintReport: LintReportModel,
        channel: SlackChannelId,
        channelForLintBugs: SlackChannelId,
        buildUrl: HttpUrl
    )

    class Impl(
        private val slackClient: SlackClient,
        loggerFactory: LoggerFactory
    ) : LintSlackReporter {

        private val logger = loggerFactory.create<LintSlackReporter>()

        override fun report(
            lintReport: LintReportModel,
            channel: SlackChannelId,
            channelForLintBugs: SlackChannelId,
            buildUrl: HttpUrl
        ) {
            when (lintReport) {
                is LintReportModel.Valid -> {

                    val shouldBeAlerted = lintReport.issues.filter { it.shouldBeAlerted() }
                    val shouldBeMentionedInAlert = lintReport.issues.filter { it.shouldBeMentionedInAlert() }

                    var isMessageSent = false

                    if (shouldBeAlerted.isNotEmpty()) {
                        sendReport(
                            channelId = channel,
                            message = buildSlackMessage(
                                projectPath = lintReport.projectRelativePath,
                                errors = shouldBeAlerted,
                                warnings = shouldBeMentionedInAlert,
                                buildUrl = buildUrl
                            ),
                            htmlReport = lintReport.htmlFile
                        )

                        isMessageSent = true
                    }

                    val shouldBeReportedAsUnexpectedProblem =
                        lintReport.issues.filter { it.shouldBeReportedAsUnexpectedProblem() }

                    val shouldBeReportedAsParseError = lintReport.issues.filter { it.shouldBeReportedAsParseError() }

                    if (shouldBeReportedAsUnexpectedProblem.isNotEmpty() || shouldBeReportedAsParseError.isNotEmpty()) {
                        sendReport(
                            channelId = channelForLintBugs,
                            message = buildSlackMessageAboutLintBugs(
                                projectPath = lintReport.projectRelativePath,
                                fatalErrors = shouldBeReportedAsUnexpectedProblem,
                                unknownErrors = shouldBeReportedAsParseError,
                                buildUrl = buildUrl
                            ),
                            htmlReport = lintReport.htmlFile
                        )

                        isMessageSent = true
                    }

                    if (!isMessageSent) {
                        logger.debug("Not sending any reports")
                    }
                }
                is LintReportModel.Invalid ->
                    // todo send this to slack also
                    logger.critical("Not sending report: can't parse", lintReport.error)
            }
        }

        private fun sendReport(
            channelId: SlackChannelId,
            message: String,
            htmlReport: File
        ) {
            slackClient.uploadHtml(
                channelId = channelId,
                message = message,
                file = htmlReport
            ).fold(
                { logger.debug("Report sent successfully to $channelId") },
                { error -> logger.critical("Can't send report to $channelId", error) }
            )
        }

        private fun buildSlackMessage(
            projectPath: String,
            errors: List<LintIssue>,
            warnings: List<LintIssue>,
            buildUrl: HttpUrl
        ): String {
            return buildString {
                appendLine("*Critical lint problems detected for project $projectPath*")
                appendLine("Build: <$buildUrl|link>")
                appendLine()

                val groupedErrors = errors.groupBy { it.summary }
                groupedErrors.forEach { (summary, issue) ->
                    appendLine(":red_circle: [${issue.size}x] $summary")
                }
                appendLine()
                if (warnings.isEmpty()) {
                    appendLine(":green_flag: No warnings!")
                } else {
                    appendLine(":warning: also ${warnings.count()} warnings")
                }
            }
        }

        private fun buildSlackMessageAboutLintBugs(
            projectPath: String,
            fatalErrors: List<LintIssue>,
            unknownErrors: List<LintIssue>,
            buildUrl: HttpUrl
        ): String {
            return buildString {
                appendLine("*Lint encountered a problem on project $projectPath*")
                appendLine("Build: <$buildUrl|link>")
                appendLine()

                fatalErrors
                    .groupBy { it.summary }
                    .forEach { (summary, issue) ->
                        appendLine(":skull: [${issue.size}x] $summary")
                    }

                if (unknownErrors.isNotEmpty()) {
                    appendLine(":alien: [${unknownErrors.size}] Unknown type issues")
                }
            }
        }

        private fun LintIssue.shouldBeAlerted(): Boolean {
            return !isFatal && severity in arrayOf(ERROR)
        }

        private fun LintIssue.shouldBeMentionedInAlert(): Boolean {
            return !isFatal && severity in arrayOf(WARNING, INFORMATION)
        }

        private fun LintIssue.shouldBeReportedAsUnexpectedProblem(): Boolean {
            return isFatal
        }

        private fun LintIssue.shouldBeReportedAsParseError(): Boolean {
            return severity == UNKNOWN
        }
    }
}
