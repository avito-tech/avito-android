package com.avito.android.string_transform.internal.report

import com.avito.android.Result
import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

internal class TransformReportJsonWriterTest {

    @Test
    fun `report json writer - writes mandatory sections - when report is serialized`(@TempDir tempDir: File) {
        val report = TransformReport(
            pipelineName = "alpha",
            variantName = "release",
            artifact = TransformReport.Artifact(
                moduleIdentity = ":app",
                variantIdentity = "release",
            ),
            rules = TransformReport.Rules(
                totalRules = 2,
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = 1,
                    caseExpanded = 1,
                )
            ),
            phases = listOf(
                TransformReport.Phase(
                    name = "stub-processing",
                    status = ReportPhaseStatus.SUCCESS,
                    durationMillis = 1,
                )
            ),
            diagnostics = listOf(
                TransformReport.Diagnostic(
                    severity = ReportDiagnosticSeverity.WARNING,
                    message = "warning message",
                    affectedPhase = null,
                    affectedPath = null,
                )
            ),
        )
        val outputFile = File(tempDir, "transform-report.json")

        val result = TransformReportJsonWriter.write(report, outputFile)

        assertThat(result.isSuccess()).isTrue()
        assertReportJsonEquals(
            outputFile,
            """
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 2,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 1
                    }
                },
                "phases": [
                    {
                        "name": "stub-processing",
                        "status": "SUCCESS",
                        "durationMillis": 1
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "WARNING",
                        "message": "warning message",
                        "affectedPhase": null,
                        "affectedPath": null
                    }
                ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `report json writer - writes failed phase and hard failure diagnostic - when failed report is serialized`(
        @TempDir tempDir: File,
    ) {
        val recorder = TransformReportRecorder(
            modulePath = ":app",
            pipelineName = "alpha",
            variantName = "release",
            rules = TransformReport.Rules(
                totalRules = 1,
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = 1,
                    caseExpanded = 0,
                )
            ),
            configurationWarnings = emptyList(),
            timeSource = FakeMonotonicTimeSource(TimeUnit.SECONDS.toNanos(1)),
        )

        val recordResult: Result<Unit> = recorder.recordPhase("stub-processing") {
            Result.Failure<Unit>(IllegalStateException("boom"))
        }

        val outputFile = File(tempDir, "transform-report.json")
        val result = TransformReportJsonWriter.write(recorder.build(), outputFile)

        assertThat(recordResult.isFailure()).isTrue()
        assertThat(result.isSuccess()).isTrue()
        assertReportJsonEquals(
            outputFile,
            """
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "stub-processing",
                        "status": "FAILURE",
                        "durationMillis": 1000
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "boom",
                        "affectedPhase": "stub-processing",
                        "affectedPath": null
                    }
                ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `report json writer - returns failure - when output path points to directory`(@TempDir tempDir: File) {
        val report = TransformReport(
            pipelineName = "alpha",
            variantName = "release",
            artifact = TransformReport.Artifact(
                moduleIdentity = ":app",
                variantIdentity = "release",
            ),
            rules = TransformReport.Rules(
                totalRules = 1,
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = 1,
                    caseExpanded = 0,
                )
            ),
            phases = emptyList(),
            diagnostics = emptyList(),
        )
        val outputDirectory = File(tempDir, "transform-report.json").apply {
            mkdirs()
        }

        val result = TransformReportJsonWriter.write(report, outputDirectory)

        assertThat(result.isFailure()).isTrue()
    }

    private fun assertReportJsonEquals(file: File, expectedJson: String) {
        assertThat(file.readText().trim()).isEqualTo(expectedJson)
    }
}
