package com.avito.android.string_transform.internal.report

import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

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

        val json = outputFile.readText()
        assertThat(result.isSuccess()).isTrue()
        assertThat(json).contains("\"artifact\"")
        assertThat(json).contains("\"rules\"")
        assertThat(json).contains("\"phases\"")
        assertThat(json).contains("\"diagnostics\"")
        assertThat(json).contains("\"pipeline\": \"alpha\"")
        assertThat(json).contains("\"variant\": \"release\"")
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
        )

        recorder.recordPhase("stub-processing") {
            error("boom")
        }

        val outputFile = File(tempDir, "transform-report.json")
        val result = TransformReportJsonWriter.write(recorder.build(), outputFile)

        val json = outputFile.readText()
        assertThat(result.isSuccess()).isTrue()
        assertThat(json).contains("\"status\": \"FAILURE\"")
        assertThat(json).contains("\"severity\": \"HARD_FAILURE\"")
        assertThat(json).contains("\"message\": \"boom\"")
        assertThat(json).contains("\"affectedPhase\": \"stub-processing\"")
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
}
