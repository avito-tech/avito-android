package com.avito.android.string_transform.internal.report

import com.avito.android.Result
import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

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
            timeSource = FakeMonotonicTimeSource(TimeUnit.SECONDS.toNanos(1)),
        )

        val result: Result<Unit> = recorder.recordPhase("stub-processing") {
            Result.Success(Unit)
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
        assertThat(report.phases.single().durationMillis).isEqualTo(1000)
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
            timeSource = FakeMonotonicTimeSource(TimeUnit.SECONDS.toNanos(1)),
        )

        val result: Result<Unit> = recorder.recordPhase("stub-processing") {
            Result.Failure<Unit>(IllegalStateException("boom"))
        }

        val report = recorder.build()

        assertThat(result.isFailure()).isTrue()
        assertThat(report.phases).hasSize(1)
        assertThat(report.phases.single().status).isEqualTo(ReportPhaseStatus.FAILURE)
        assertThat(report.phases.single().durationMillis).isEqualTo(1000)
        assertThat(report.diagnostics).hasSize(1)
        assertThat(report.diagnostics.single().severity).isEqualTo(ReportDiagnosticSeverity.HARD_FAILURE)
        assertThat(report.diagnostics.single().affectedPhase).isEqualTo("stub-processing")
        assertThat(report.diagnostics.single().message).isEqualTo("boom")
    }
}
