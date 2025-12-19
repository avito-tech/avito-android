package com.avito.android.test_results_upload

import com.avito.android.http.nupokati.NupokatiV4ClientBuildService
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
    internal abstract val cdBuildConfig: Property<CdBuildConfigV4>

    @get:Input
    internal abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    internal abstract val reportViewerUrl: Property<String>

    @get:Input
    internal abstract val appVersionCode: Property<Int>

    @TaskAction
    fun sendTestResults() {
        val config = cdBuildConfig.get()

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
        ).fold(
            onSuccess = { logger.lifecycle("Successfully uploaded test results - ${it.message}") },
            onFailure = { throwable ->
                logger.error("Failed to upload test results", throwable)
                throw throwable
            }
        )
    }
}
