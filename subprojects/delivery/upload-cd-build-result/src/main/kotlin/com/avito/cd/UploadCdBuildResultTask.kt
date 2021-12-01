package com.avito.cd

import com.avito.android.androidAppExtension
import com.avito.git.gitState
import com.avito.utils.gradle.envArgs
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public abstract class UploadCdBuildResultTask : DefaultTask() {

    @get:Input
    public abstract val artifactoryUser: Property<String>

    @get:Input
    public abstract val artifactoryPassword: Property<String>

    @get:Input
    public abstract val suppressErrors: Property<Boolean>

    @get:Input
    public abstract val reportUrl: Property<String>

    @get:Input
    public abstract val reportCoordinates: Property<CdBuildResult.TestResultsLink.ReportCoordinates>

    @TaskAction
    public fun sendCdBuildResult() {
        val gitState = project.gitState()
        createUploadAction().send(
            testResults = CdBuildResult.TestResultsLink(
                reportUrl = reportUrl.get(),
                reportCoordinates = reportCoordinates.get()
            ),
            buildOutput = project.buildOutput.get(),
            cdBuildConfig = project.cdBuildConfig.get(),
            versionCode = project.androidAppExtension.defaultConfig.versionCode.toString(),
            teamcityUrl = project.envArgs.build.url,
            gitState = gitState.get(),
        )
    }

    private fun createUploadAction(): UploadCdBuildResultTaskAction {
        return UploadCdBuildResultTaskAction(
            gson = uploadCdGson,
            client = Providers.client(
                user = artifactoryUser.get(),
                password = artifactoryPassword.get(),
            ),
            suppressErrors = suppressErrors.get()
        )
    }
}
