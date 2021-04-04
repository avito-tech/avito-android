package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.utils.createOrClear
import okhttp3.HttpUrl
import java.io.File

internal class WriteReportViewerLinkFile(
    private val reportViewer: ReportViewer,
    private val reportCoordinates: ReportCoordinates,
    private val outputDir: File
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        val reportUrl = reportViewer.generateReportUrl(
            reportCoordinates,
            onlyFailures = testRunResult.verdict is TestRunResult.Verdict.Failure
        )
        writeReportViewerLinkFile(
            reportUrl,
            reportViewerFile(outputDir)
        )
    }

    /**
     * Teamcity report tab
     */
    private fun reportViewerFile(outputDir: File): File = File(outputDir, "rv.html")

    private fun writeReportViewerLinkFile(
        reportViewerUrl: HttpUrl,
        reportFile: File
    ) {
        reportFile.createOrClear()
        reportFile.writeText("<script>location=\"${reportViewerUrl}\"</script>")
    }
}
