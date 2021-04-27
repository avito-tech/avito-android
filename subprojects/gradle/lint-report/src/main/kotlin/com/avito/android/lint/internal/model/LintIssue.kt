package com.avito.android.lint.internal.model

internal class LintIssue(
    val id: String,
    val summary: String,
    val message: String,
    val path: String,
    val line: Int,
    val severity: Severity
) {
    enum class Severity { UNKNOWN, WARNING, ERROR, INFORMATION }

    /**
     * "lint failed to parse file" type of errors
     */
    @Suppress("UnnecessaryParentheses")
    val isFatal: Boolean = (id == "LintError") || message.startsWith("Check failed")
}
