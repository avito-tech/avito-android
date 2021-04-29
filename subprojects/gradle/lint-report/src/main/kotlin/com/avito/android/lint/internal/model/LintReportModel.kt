package com.avito.android.lint.internal.model

import java.io.File

internal sealed class LintReportModel(
    val projectRelativePath: String,
    val htmlFile: File
) {

    class Valid(
        projectRelativePath: String,
        htmlFile: File,
        val issues: List<LintIssue>
    ) : LintReportModel(projectRelativePath, htmlFile)

    class Invalid(
        projectRelativePath: String,
        htmlFile: File,
        val error: Exception
    ) : LintReportModel(projectRelativePath, htmlFile)
}
