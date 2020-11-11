package com.avito.android.lint.slack

import com.avito.android.lint.model.LintIssue
import com.avito.android.lint.model.LintReportModel
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.CILogger

interface LintSlackReporter {

    fun report(
        lintReport: LintReportModel,
        channel: SlackChannel
    )

    class Impl(
        private val slackClient: SlackClient,
        private val logger: CILogger
    ) : LintSlackReporter {

        private val tag = "LintSlackAlert"

        override fun report(
            lintReport: LintReportModel,
            channel: SlackChannel
        ) {
            if (shouldSendAlert(lintReport)) {
                logger.debug("$tag: Sending lint alert...")

                slackClient.uploadHtml(
                    channel = channel,
                    message = buildSlackMessage(lintReport),
                    file = lintReport.htmlFile
                ).fold(
                    { logger.debug("$tag: Report sent successfully") },
                    { error -> logger.critical("$tag: Can't send report ${error.message}") }
                )
            } else {
                logger.debug("$tag Skip sending lint alert")
            }
        }

        private fun shouldSendAlert(model: LintReportModel): Boolean {
            return (model is LintReportModel.Valid) && model.issues.any { it.severity == LintIssue.Severity.ERROR }
        }

        private fun buildSlackMessage(model: LintReportModel): String {
            return buildString {
                appendln("*Critical lint problems detected for project ${model.projectRelativePath}!*")
                appendln()

                if (model is LintReportModel.Valid) {
                    val errors = model.issues.filter { it.severity == LintIssue.Severity.ERROR }
                    val groupedErrors = errors.groupBy { it.summary }
                    groupedErrors.forEach { (summary, issue) ->
                        appendln(":red_circle: [${issue.size}x] $summary")
                    }
                    appendln(":warning: also ${model.issues.count { it.severity == LintIssue.Severity.WARNING }} warnings")
                } else {
                    logger.critical("LintSlackAlerter: There is a problem with report: ${model.htmlFile.path}")
                }
            }
        }
    }
}
