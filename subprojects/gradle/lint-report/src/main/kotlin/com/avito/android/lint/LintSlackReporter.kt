package com.avito.android.lint

import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.CILogger

interface LintSlackReporter {

    fun report(
        models: List<LintReportModel>,
        channel: SlackChannel
    )

    class Impl(
        private val slackClient: SlackClient,
        private val logger: CILogger
    ) : LintSlackReporter {

        private val tag = "LintSlackAlert"

        override fun report(
            models: List<LintReportModel>,
            channel: SlackChannel
        ) {
            models.forEach { model ->
                if (shouldSendAlert(model)) {
                    logger.debug("$tag: Sending lint alert...")

                    slackClient.uploadHtml(
                        channel = channel,
                        message = buildSlackMessage(model),
                        file = model.htmlFile
                    ).fold(
                        { logger.debug("$tag: Report sent successfully") },
                        { error -> logger.critical("$tag: Can't send report ${error.message}") }
                    )
                } else {
                    logger.debug("$tag Skip sending lint alert")
                }
            }
        }

        private fun shouldSendAlert(model: LintReportModel): Boolean {
            return (model is LintReportModel.Valid) && model.issues.any { it.severity == LintIssue.Severity.ERROR }
        }

        private fun buildSlackMessage(model: LintReportModel): String {
            return buildString {
                appendln("*Critical lint problems detected in develop!*")
                appendln("for project: ${model.projectRelativePath}")

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
