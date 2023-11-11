package com.avito.instrumentation.runner.input_params

import com.avito.instrumentation.configuration.report.ReportConfig.ReportViewer.SendFromRunner
import com.avito.instrumentation.kotlinStubConfig
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.RunId
import com.avito.runner.config.RunnerReportConfig
import java.io.File

class SendFromRunnerCase(
    override val projectDir: File,
) : Case() {

    private inner class Then(
        commit: String
    ) : Case.Then(
        "${projectDir.canonicalPath}/outputs/$commit.teamcity-$buildType/$configurationName"
    ) {

        private val runId = RunId(
            identifier = commit,
            buildTypeId = "teamcity-$buildType"
        ).toReportViewerFormat()

        override val expectedPluginInstrumentationParams = mapOf(
            "configuration" to configurationName,
            "override" to "overrideInConfiguration",
            "expectedCustomParam" to "value",
            "avito.report.transport" to "legacy",
            "fileStorageUrl" to "http://stub",
        )

        override val expectedReportConfig: RunnerReportConfig = RunnerReportConfig.ReportViewer(
            reportApiUrl = "http://stub",
            reportViewerUrl = "http://stub",
            fileStorageUrl = "http://stub",
            coordinates = ReportCoordinates(
                planSlug = "AppAndroid",
                jobSlug = "override jobSlug",
                runId = runId
            )
        )
    }

    override val buildScript: String
        get() = kotlinStubConfig(reportConfig)

    override val reportConfig: SendFromRunner = SendFromRunner(
        reportApiUrl = "http://stub",
        reportViewerUrl = "http://stub",
        fileStorageUrl = "http://stub",
        planSlug = "AppAndroid",
        jobSlug = "FunctionalTests",
    )

    override fun createThen(commit: String): Case.Then {
        return Then(commit)
    }
}
