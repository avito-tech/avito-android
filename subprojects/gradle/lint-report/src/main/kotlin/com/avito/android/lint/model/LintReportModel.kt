package com.avito.android.lint.model

import java.io.File

sealed class LintReportModel(
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

internal fun LintReportModel.hasErrors(): Boolean {
    return when {
        this is LintReportModel.Invalid -> true
        this is LintReportModel.Valid &&
            (issues.any { it.severity == LintIssue.Severity.ERROR }) -> true
        else -> false
    }
}
