package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.report.ReportLinkGenerator
import com.avito.utils.createOrClear
import java.io.File

internal class WriteReportViewerLinkFile(
    private val outputDir: File,
    private val reportLinkGenerator: ReportLinkGenerator
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        val reportUrl = reportLinkGenerator.generateReportLink(
            filterOnlyFailtures = testRunResult.verdict is TestRunResult.Verdict.Failure
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
