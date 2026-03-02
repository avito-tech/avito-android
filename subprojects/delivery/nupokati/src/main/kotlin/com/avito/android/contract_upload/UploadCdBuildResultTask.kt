package com.avito.android.contract_upload

import com.avito.android.artifactory_backup.ArtifactsAdapter
import com.avito.android.http.artifactory.ArtifactoryClient
import com.avito.android.http.artifactory.createArtifactoryHttpClient
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.model.output.CdBuildResult
import com.avito.android.model.output.toCdCoordinates
import com.avito.git.gitStateProvider
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

public abstract class UploadCdBuildResultTask : DefaultTask() {

    @get:Input
    public abstract val artifactoryUser: Property<String>

    @get:Input
    public abstract val artifactoryPassword: Property<String>

    @get:Input
    public abstract val reportViewerUrl: Property<String>

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    public abstract val teamcityBuildUrl: Property<String>

    @get:Input
    internal abstract val cdBuildConfig: Property<CdBuildConfigV2>

    @get:Input
    public abstract val appVersionCode: Property<Int>

    @get:InputFiles
    public abstract val buildOutputFiles: ConfigurableFileCollection

    @TaskAction
    public fun sendCdBuildResult() {
        val gitState = project.gitStateProvider()

        val reportLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl = reportViewerUrl.get(),
            reportCoordinates = reportCoordinates.get(),
            reportViewerQuery = ReportViewerQuery.createForJvm()
        )

        val cdBuildConfig = cdBuildConfig.get()

        val artifactsAdapter = ArtifactsAdapter(cdBuildConfig.schemaVersion)

        val artifacts = buildOutputFiles.files.flatMap { artifactsAdapter.fromJson(it.readText()) }
        val artifactsJson = artifactsAdapter.toJsonElement(artifacts)

        createUploadAction().send(
            testResults = CdBuildResult.TestResultsLink(
                reportUrl = reportLinksGenerator.generateReportLink(filterOnlyFailures = false),
                reportCoordinates = reportCoordinates.get().toCdCoordinates()
            ),
            artifacts = artifactsJson,
            cdBuildConfig = cdBuildConfig,
            versionCode = appVersionCode.get(),
            teamcityUrl = teamcityBuildUrl.get(),
            gitState = gitState.get(),
        )
    }

    private fun createUploadAction(): UploadCdBuildResultTaskAction = UploadCdBuildResultTaskAction(
        client = ArtifactoryClient(
            createArtifactoryHttpClient(
                user = artifactoryUser.get(),
                password = artifactoryPassword.get(),
            )
        )
    )
}
