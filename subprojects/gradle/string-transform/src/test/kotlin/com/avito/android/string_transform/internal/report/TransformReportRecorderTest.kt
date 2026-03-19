package com.avito.android.string_transform.internal.report

import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TransformReportRecorderTest {

    @Test
    fun `report recorder - records successful phase and configuration warnings - when report is built`() {
        val recorder = TransformReportRecorder(
            modulePath = ":app",
            pipelineName = "alpha",
            variantName = "release",
            rules = TransformReport.Rules(
                totalRules = 3,
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = 1,
                    caseExpanded = 1,
                )
            ),
            configurationWarnings = listOf("warning message"),
        )

        val result = recorder.recordPhase("stub-processing") {
        }

        val report = recorder.build()

        assertThat(result.isSuccess()).isTrue()
        assertThat(report.pipelineName).isEqualTo("alpha")
        assertThat(report.variantName).isEqualTo("release")
        assertThat(report.artifact.moduleIdentity).isEqualTo(":app")
        assertThat(report.rules.totalRules).isEqualTo(3)
        assertThat(report.phases).hasSize(1)
        assertThat(report.phases.single().name).isEqualTo("stub-processing")
        assertThat(report.phases.single().status).isEqualTo(ReportPhaseStatus.SUCCESS)
        assertThat(report.diagnostics).hasSize(1)
        assertThat(report.diagnostics.single().severity).isEqualTo(ReportDiagnosticSeverity.WARNING)
        assertThat(report.diagnostics.single().message).isEqualTo("warning message")
    }

    @Test
    fun `report recorder - records failed phase and hard failure diagnostic - when phase action throws`() {
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

        val result = recorder.recordPhase("stub-processing") {
            error("boom")
        }

        val report = recorder.build()

        assertThat(result.isFailure()).isTrue()
        assertThat(report.phases).hasSize(1)
        assertThat(report.phases.single().status).isEqualTo(ReportPhaseStatus.FAILURE)
        assertThat(report.diagnostics).hasSize(1)
        assertThat(report.diagnostics.single().severity).isEqualTo(ReportDiagnosticSeverity.HARD_FAILURE)
        assertThat(report.diagnostics.single().affectedPhase).isEqualTo("stub-processing")
        assertThat(report.diagnostics.single().message).isEqualTo("boom")
    }
}
