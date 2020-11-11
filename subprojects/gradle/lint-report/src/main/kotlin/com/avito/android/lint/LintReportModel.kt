package com.avito.android.lint

import java.io.File

sealed class LintReportModel(val projectRelativePath: String, val htmlFile: File) {

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

class LintIssue(
    val id: String,
    val summary: String,
    val message: String,
    val path: String,
    val line: Int,
    val severity: Severity
) {
    enum class Severity { UNKNOWN, WARNING, ERROR }
}

internal fun LintReportModel.hasErrors(): Boolean {
    return when {
        this is LintReportModel.Invalid -> true
        this is LintReportModel.Valid &&
            (issues.any { it.severity == LintIssue.Severity.ERROR }) -> true
        else -> false
    }
}
