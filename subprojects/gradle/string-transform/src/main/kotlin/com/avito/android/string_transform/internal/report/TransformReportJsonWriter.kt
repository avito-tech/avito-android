package com.avito.android.string_transform.internal.report

import com.avito.android.Result
import groovy.json.JsonOutput
import java.io.File

internal object TransformReportJsonWriter {

    fun write(report: TransformReport, outputFile: File): Result<Unit> = Result.tryCatch {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            JsonOutput.prettyPrint(
                JsonOutput.toJson(
                    mapOf(
                        "pipeline" to report.pipelineName,
                        "variant" to report.variantName,
                        "artifact" to mapOf(
                            "moduleIdentity" to report.artifact.moduleIdentity,
                            "variantIdentity" to report.artifact.variantIdentity,
                        ),
                        "rules" to mapOf(
                            "totalRules" to report.rules.totalRules,
                            "declarationCounts" to mapOf(
                                "exact" to report.rules.declarationCounts.exact,
                                "caseExpanded" to report.rules.declarationCounts.caseExpanded,
                            ),
                        ),
                        "phases" to report.phases.map { phase ->
                            mapOf(
                                "name" to phase.name,
                                "status" to phase.status.name,
                                "durationMillis" to phase.durationMillis,
                            )
                        },
                        "diagnostics" to report.diagnostics.map { diagnostic ->
                            mapOf(
                                "severity" to diagnostic.severity.name,
                                "message" to diagnostic.message,
                                "affectedPhase" to diagnostic.affectedPhase,
                                "affectedPath" to diagnostic.affectedPath,
                            )
                        },
                    )
                )
            )
        )
    }
}
