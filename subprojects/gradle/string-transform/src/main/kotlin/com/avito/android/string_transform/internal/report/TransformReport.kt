package com.avito.android.string_transform.internal.report

internal data class TransformReport(
    val pipelineName: String,
    val variantName: String,
    val artifact: Artifact,
    val rules: Rules,
    val phases: List<Phase>,
    val diagnostics: List<Diagnostic>,
) {

    internal data class Artifact(
        val moduleIdentity: String,
        val variantIdentity: String,
    )

    internal data class Rules(
        val totalRules: Int,
        val declarationCounts: DeclarationCounts,
    )

    internal data class DeclarationCounts(
        val exact: Int,
        val caseExpanded: Int,
    )

    internal data class Phase(
        val name: String,
        val status: ReportPhaseStatus,
        val durationMillis: Long,
    )

    internal data class Diagnostic(
        val severity: ReportDiagnosticSeverity,
        val message: String,
        val affectedPhase: String?,
        val affectedPath: String?,
    )
}
