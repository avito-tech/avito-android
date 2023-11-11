package com.avito.instrumentation.runner.input_params

import com.avito.instrumentation.configuration.report.ReportConfig.ReportViewer.SendFromDevice
import com.avito.instrumentation.kotlinStubConfig
import com.avito.reportviewer.model.RunId
import com.avito.runner.config.RunnerReportConfig
import java.io.File

class SendFromDeviceCase(
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
            "avito.report.transport" to "backend",
            "fileStorageUrl" to "http://stub",
            "reportViewerUrl" to "http://stub",
            "reportApiUrl" to "http://stub",
            "planSlug" to "AppAndroid",
            "jobSlug" to "override jobSlug",
            "runId" to runId,
            "deviceName" to "local",
        )

        override val expectedReportConfig: RunnerReportConfig = RunnerReportConfig.None
    }

    override val buildScript: String
        get() = kotlinStubConfig(reportConfig)

    override val reportConfig: SendFromDevice = SendFromDevice(
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
