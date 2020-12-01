package com.avito.android.lint.slack

import com.avito.android.lint.model.LintIssue
import com.avito.android.lint.model.LintIssue.Severity.ERROR
import com.avito.android.lint.model.LintIssue.Severity.INFORMATION
import com.avito.android.lint.model.LintIssue.Severity.UNKNOWN
import com.avito.android.lint.model.LintIssue.Severity.WARNING
import com.avito.android.lint.model.LintReportModel
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.CILogger
import okhttp3.HttpUrl
import java.io.File

interface LintSlackReporter {

    fun report(
        lintReport: LintReportModel,
        channel: SlackChannel,
        channelForLintBugs: SlackChannel,
        buildUrl: HttpUrl
    )

    class Impl(
        private val slackClient: SlackClient,
        private val logger: CILogger
    ) : LintSlackReporter {

        private val tag = "LintSlackAlert"

        override fun report(
            lintReport: LintReportModel,
            channel: SlackChannel,
            channelForLintBugs: SlackChannel,
            buildUrl: HttpUrl
        ) {
            when (lintReport) {
                is LintReportModel.Valid -> {

                    val shouldBeAlerted = lintReport.issues.filter { it.shouldBeAlerted() }
                    val shouldBeMentionedInAlert = lintReport.issues.filter { it.shouldBeMentionedInAlert() }

                    var isMessageSent = false

                    if (shouldBeAlerted.isNotEmpty()) {
                        sendReport(
                            channel = channel,
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
                            channel = channelForLintBugs,
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
                        logger.debug("$tag Not sending any reports")
                    }
                }
                is LintReportModel.Invalid ->
                    //todo send this to slack also
                    logger.critical("$tag Not sending report: can't parse", lintReport.error)
            }
        }

        private fun sendReport(
            channel: SlackChannel,
            message: String,
            htmlReport: File
        ) {
            slackClient.uploadHtml(
                channel = channel,
                message = message,
                file = htmlReport
            ).fold(
                { logger.debug("$tag: Report sent successfully to $channel") },
                { error -> logger.critical("$tag: Can't send report to $channel", error) }
            )
        }

        private fun buildSlackMessage(
            projectPath: String,
            errors: List<LintIssue>,
            warnings: List<LintIssue>,
            buildUrl: HttpUrl
        ): String {
            return buildString {
                appendln("*Critical lint problems detected for project $projectPath*")
                appendln("Build: <$buildUrl|link>")
                appendln()

                val groupedErrors = errors.groupBy { it.summary }
                groupedErrors.forEach { (summary, issue) ->
                    appendln(":red_circle: [${issue.size}x] $summary")
                }
                appendln()
                if (warnings.isEmpty()) {
                    appendln(":green_flag: No warnings!")
                } else {
                    appendln(":warning: also ${warnings.count()} warnings")
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
                appendln("*Lint encountered a problem on project $projectPath*")
                appendln("Build: <$buildUrl|link>")
                appendln()

                fatalErrors
                    .groupBy { it.summary }
                    .forEach { (summary, issue) ->
                        appendln(":skull: [${issue.size}x] $summary")
                    }

                if (unknownErrors.isNotEmpty()) {
                    appendln(":alien: [${unknownErrors.size}] Unknown type issues")
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
