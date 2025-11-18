package com.avito.android.test_results_upload

import com.avito.android.http.nupokati.NupokatiV4ClientBuildService
import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.CdBuildConfigV4
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

internal abstract class SendTestResultsTask : DefaultTask() {

    @get:Internal
    internal abstract val nupokatiClientService: Property<NupokatiV4ClientBuildService>

    @get:Input
    internal abstract val cdBuildConfig: Property<CdBuildConfig>

    @get:Input
    internal abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    internal abstract val reportViewerUrl: Property<String>

    @get:Input
    internal abstract val appVersionCode: Property<Int>

    @TaskAction
    fun sendTestResults() {
        val config = cdBuildConfig.get()
        require(config is CdBuildConfigV4) {
            "UploadArtifactsTask supported only for CdBuildConfig with schema version 4"
        }

        val client = nupokatiClientService.get().getClient()

        val reportLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl = reportViewerUrl.get(),
            reportCoordinates = reportCoordinates.get(),
            reportViewerQuery = ReportViewerQuery.createForJvm()
        )

        client.saveTestResult(
            project = config.project,
            platform = "android",
            version = config.releaseVersion,
            buildNumber = appVersionCode.get(),
            reportUrl = reportLinksGenerator.generateReportLink(filterOnlyFailures = false),
            reportCoordinates = reportCoordinates.get()
        )
    }
}
