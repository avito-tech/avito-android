package com.avito.runner.finalizer.action

import com.avito.report.ReportLinksGenerator
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.utils.createOrClear
import java.io.File

internal class WriteReportViewerLinkFile(
    private val outputDir: File,
    private val reportLinksGenerator: ReportLinksGenerator
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        val reportUrl = reportLinksGenerator.generateReportLink(
            filterOnlyFailures = verdict is Verdict.Failure
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
