package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdict
import com.avito.report.ReportLinkGenerator
import com.avito.utils.createOrClear
import java.io.File

internal class LegacyWriteReportViewerLinkFile(
    private val outputDir: File,
    private val reportLinkGenerator: ReportLinkGenerator
) : LegacyFinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: LegacyVerdict) {
        val reportUrl = reportLinkGenerator.generateReportLink(
            filterOnlyFailtures = verdict is LegacyVerdict.Failure
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
        reportViewerUrl: String,
        reportFile: File
    ) {
        reportFile.createOrClear()
        reportFile.writeText("<script>location=\"$reportViewerUrl\"</script>")
    }
}
