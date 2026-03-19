package com.avito.android.string_transform.internal.report

import com.avito.android.Result

internal class TransformReportRecorder(
    private val modulePath: String,
    private val pipelineName: String,
    private val variantName: String,
    private val rules: TransformReport.Rules,
    configurationWarnings: List<String>,
) {

    private val phases = mutableListOf<TransformReport.Phase>()
    private val diagnostics = configurationWarnings
        .map { warning ->
            TransformReport.Diagnostic(
                severity = ReportDiagnosticSeverity.WARNING,
                message = warning,
                affectedPhase = null,
                affectedPath = null,
            )
        }
        .toMutableList()

    fun recordPhase(name: String, action: () -> Unit): Result<Unit> {
        val startNanos = System.nanoTime()
        return Result.tryCatch {
            action()
        }.onSuccess {
            phases += TransformReport.Phase(
                name = name,
                status = ReportPhaseStatus.SUCCESS,
                durationMillis = elapsedMillis(startNanos),
            )
        }.onFailure { t ->
            phases += TransformReport.Phase(
                name = name,
                status = ReportPhaseStatus.FAILURE,
                durationMillis = elapsedMillis(startNanos),
            )
            diagnostics += TransformReport.Diagnostic(
                severity = ReportDiagnosticSeverity.HARD_FAILURE,
                message = t.message ?: "Execution failed",
                affectedPhase = name,
                affectedPath = null,
            )
        }
    }

    fun addWarning(
        message: String,
        affectedPhase: String? = null,
        affectedPath: String? = null,
    ) {
        diagnostics += TransformReport.Diagnostic(
            severity = ReportDiagnosticSeverity.WARNING,
            message = message,
            affectedPhase = affectedPhase,
            affectedPath = affectedPath,
        )
    }

    fun build(): TransformReport {
        return TransformReport(
            pipelineName = pipelineName,
            variantName = variantName,
            artifact = TransformReport.Artifact(
                moduleIdentity = modulePath,
                variantIdentity = variantName,
            ),
            rules = rules,
            phases = phases.toList(),
            diagnostics = diagnostics.toList(),
        )
    }

    private fun elapsedMillis(startNanos: Long): Long {
        return (System.nanoTime() - startNanos) / 1_000_000
    }
}
