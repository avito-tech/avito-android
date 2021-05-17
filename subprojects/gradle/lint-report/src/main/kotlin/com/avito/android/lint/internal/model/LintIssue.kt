package com.avito.android.lint.internal.model

internal class LintIssue(
    val id: String,
    val summary: String,
    val message: String,
    val path: String,
    val line: Int,
    val severity: Severity?
) {

    /**
     * "lint failed to parse file" type of errors
     */
    @Suppress("UnnecessaryParentheses")
    val isFatal: Boolean = (id == "LintError") || message.startsWith("Check failed")
}
