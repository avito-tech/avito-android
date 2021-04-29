package com.avito.android.lint.internal.model

import com.android.tools.lint.detector.api.Severity

internal class LintIssue(
    val id: String,
    val summary: String,
    val message: String,
    val path: String,
    val line: Int,
    @Suppress("UnstableApiUsage")
    val severity: Severity?
) {

    /**
     * "lint failed to parse file" type of errors
     */
    @Suppress("UnnecessaryParentheses")
    val isFatal: Boolean = (id == "LintError") || message.startsWith("Check failed")
}
