package com.avito.android.lint.report

internal class DependenciesReport(val issues: List<LintIssue>)

internal interface LintIssue {
    val id: String
    val severity: Severity
    val message: String
    val summary: String
}

internal enum class Severity {
    error, warning, informational, ignore
}

internal sealed class BaseLintIssue: LintIssue

internal data class UnusedDependency(
    override val severity: Severity,
    override val message: String,
    override val summary: String
) : BaseLintIssue() {
    override val id: String = UNUSED_DEPENDENCY_ID
}

internal data class RedundantDependency(
    override val severity: Severity,
    override val message: String,
    override val summary: String
) : BaseLintIssue() {
    override val id: String = REDUNDANT_DEPENDENCY_ID
}

internal const val UNUSED_DEPENDENCY_ID = "UnusedDependency"
internal const val REDUNDANT_DEPENDENCY_ID = "RedundantDependency"
