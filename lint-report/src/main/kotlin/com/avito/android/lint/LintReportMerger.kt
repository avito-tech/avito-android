package com.avito.android.lint

import kotlinx.html.HtmlBlockTag
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe
import java.io.File

internal class LintReportMerger(
    private val reports: List<LintReportModel>,
    private val mergedReport: File
) {

    fun write() {
        val document = createHtmlDocument(sort(reports))
        mergedReport.writeText(document)
    }

    private fun sort(models: List<LintReportModel>): List<LintReportModel> {
        val invalidReports = models.filterIsInstance<LintReportModel.Invalid>()
        val validReports = models.filterIsInstance<LintReportModel.Valid>()

        val sortComparator =
            compareByDescending<LintReportModel> { it.issuesDescription(LintIssue.Severity.ERROR) }
                .thenByDescending { it.issuesDescription(LintIssue.Severity.WARNING) }

        return invalidReports + validReports.sortedWith(sortComparator)
    }

    private fun createHtmlDocument(reports: List<LintReportModel>): String {
        return createHTML().html {
            head {
                meta {
                    httpEquiv = "Content-Type"
                    content = "text/html; charset=UTF-8"
                }
                title("Lint report")
                link {
                    rel = "stylesheet"
                    type = "text/css"
                    href = "https://code.getmdl.io/1.2.1/material.blue-indigo.min.css"
                }
                link {
                    rel = "stylesheet"
                    type = "text/css"
                    href = "http://fonts.googleapis.com/css?family=Roboto:300,400,500,700"
                }
                style {
                    unsafe {
                        raw(
                            """
                            .page-content {
                                padding: 20px;
                            }
                            """.trimIndent()
                        )
                    }
                }
            }
            body("mdl-color--grey-100 mdl-color-text--grey-700 mdl-base") {
                div("mdl-layout mdl-js-layout mdl-layout--fixed-header") {
                    header("mdl-layout__header") {
                        div("mdl-layout__header-row") {
                            span("mdl-layout-title") {
                                text("Lint report")
                            }
                        }
                    }
                    main("mdl-layout__content") {
                        div("page-content") {
                            generatePageContent(this, reports)
                        }
                    }
                }
            }
        }
    }

    private fun generatePageContent(container: HtmlBlockTag, reports: List<LintReportModel>) {
        container.table("mdl-data-table mdl-shadow--2dp") {
            thead {
                tr {
                    th(classes = "mdl-data-table__cell--non-numeric") {
                        text("Module")
                    }
                    th { text("Errors") }
                    th { text("Warnings") }
                }
            }
            tbody {
                reports.forEach { report ->
                    tr {
                        val classes = if (report.hasErrors()) {
                            "mdl-data-table__cell--non-numeric mdl-color--red-100"
                        } else {
                            "mdl-data-table__cell--non-numeric"
                        }
                        td(classes) {
                            a {
                                href = report.htmlFile.toRelativeString(mergedReport)
                                    .removePrefix("../")
                                text(report.projectRelativePath)
                            }
                        }
                        td(classes) { text(report.issuesDescription(LintIssue.Severity.ERROR)) }
                        td(classes) { text(report.issuesDescription(LintIssue.Severity.WARNING)) }
                    }
                }
            }
        }
    }

    private fun LintReportModel.issuesDescription(severity: LintIssue.Severity): String {
        return when (this) {
            is LintReportModel.Invalid -> "INVALID"
            is LintReportModel.Valid -> {
                issues.count { it.severity == severity }.toString()
            }
        }
    }

}
