package com.avito.instrumentation.finalizer

import com.avito.instrumentation.TestRunResult
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.report.model.ReportCoordinates
import com.avito.utils.BuildFailer
import com.avito.utils.createOrClear
import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import okhttp3.HttpUrl
import java.io.File

internal interface InstrumentationActionFinalizer {

    fun finalize(
        outputDir: File,
        reportCoordinates: ReportCoordinates,
        reportViewerUrl: HttpUrl,
        testRunResult: TestRunResult
    )

    class Impl(
        private val logger: CILogger,
        private val gson: Gson,
        private val jUnitReportWriter: JUnitReportWriter,
        private val buildFailer: BuildFailer = BuildFailer.RealFailer()
    ) : InstrumentationActionFinalizer {

        override fun finalize(
            outputDir: File,
            reportCoordinates: ReportCoordinates,
            reportViewerUrl: HttpUrl,
            testRunResult: TestRunResult
        ) {
            jUnitReportWriter.write(
                reportCoordinates = reportCoordinates,
                testRunResult = testRunResult,
                destination = junitFile(outputDir)
            )

            writeReportViewerLinkFile(
                reportViewerUrl,
                reportViewerFile(outputDir)
            )

            logger.info("Report url $reportViewerUrl")

            val verdict = testRunResult.verdict
            val verdictFile = verdictFile(outputDir)
            verdictFile.writeText(gson.toJson(verdict))

            when (verdict) {
                is TestRunResult.Verdict.Success -> logger.debug(verdict.message)
                is TestRunResult.Verdict.Failed -> buildFailer.failBuild(
                    "Instrumentation task failed. Look at verdict in the file: $verdictFile"
                )
            }
        }

        /**
         * teamcity XML report processing
         */
        private fun junitFile(outputDir: File): File = File(outputDir, "junit-report.xml")

        /**
         * teamcity report tab
         */
        private fun reportViewerFile(outputDir: File): File = File(outputDir, "rv.html")

        private fun verdictFile(outputDir: File) = File(outputDir, "verdict.json")

        private fun writeReportViewerLinkFile(
            reportViewerUrl: HttpUrl,
            reportFile: File
        ) {
            reportFile.createOrClear()
            reportFile.writeText("<script>location=\"${reportViewerUrl}\"</script>")
        }
    }
}