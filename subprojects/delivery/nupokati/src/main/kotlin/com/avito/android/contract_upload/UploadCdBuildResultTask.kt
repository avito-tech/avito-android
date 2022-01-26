package com.avito.android.contract_upload

import com.avito.android.artifactory_backup.CdBuildResultArtifactsAdapter
import com.avito.android.http.ArtifactoryClient
import com.avito.android.http.createArtifactoryHttpClient
import com.avito.android.model.CdBuildConfig
import com.avito.android.model.CdBuildResult
import com.avito.android.model.toCdCoordinates
import com.avito.android.provider.uploadCdGson
import com.avito.android.stats.StatsDConfig
import com.avito.git.gitState
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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
    public abstract val cdBuildConfig: Property<CdBuildConfig>

    @get:Input
    public abstract val appVersionCode: Property<Int>

    @get:InputFile
    public abstract val buildOutputFileProperty: RegularFileProperty

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @TaskAction
    public fun sendCdBuildResult() {
        val gitState = project.gitState()

        val reportLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl = reportViewerUrl.get(),
            reportCoordinates = reportCoordinates.get(),
            reportViewerQuery = ReportViewerQuery.createForJvm()
        )

        val buildOutputFile = buildOutputFileProperty.get().asFile

        val artifactsAdapter = CdBuildResultArtifactsAdapter()

        val buildOutput = artifactsAdapter.fromJson(buildOutputFile.readText())

        createUploadAction().send(
            testResults = CdBuildResult.TestResultsLink(
                reportUrl = reportLinksGenerator.generateReportLink(filterOnlyFailures = false),
                reportCoordinates = reportCoordinates.get().toCdCoordinates()
            ),
            buildOutput = buildOutput,
            cdBuildConfig = cdBuildConfig.get(),
            versionCode = appVersionCode.get(),
            teamcityUrl = teamcityBuildUrl.get(),
            gitState = gitState.get(),
        )
    }

    private fun createUploadAction(): UploadCdBuildResultTaskAction {
        return UploadCdBuildResultTaskAction(
            gson = uploadCdGson,
            client = ArtifactoryClient(
                createArtifactoryHttpClient(
                    user = artifactoryUser.get(),
                    password = artifactoryPassword.get(),
                    statsDConfig = statsDConfig.get()
                )
            ),
            logger = logger
        )
    }
}
