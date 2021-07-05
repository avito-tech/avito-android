package com.avito.cd

import com.avito.android.androidAppExtension
import com.avito.git.gitState
import com.avito.http.HttpLogger
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import com.avito.utils.gradle.envArgs
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public abstract class UploadCdBuildResultTask : DefaultTask() {

    @get:Input
    public abstract val user: Property<String>

    @get:Input
    public abstract val password: Property<String>

    @get:Input
    public abstract val suppressErrors: Property<Boolean>

    @get:Input
    public abstract val reportUrl: Property<String>

    @get:Input
    public abstract val planSlug: Property<String>

    @get:Input
    public abstract val jobSlug: Property<String>

    @get:Input
    public abstract val runId: Property<String>

    @TaskAction
    public fun sendCdBuildResult() {
        val gitState = project.gitState()
        val logger = GradleLoggerFactory.getLogger(this)
        createUploadAction(logger).send(
            testResults = CdBuildResult.TestResultsLink(
                reportUrl.get(),
                CdBuildResult.TestResultsLink.ReportCoordinates(
                    planSlug = planSlug.get(),
                    jobSlug = jobSlug.get(),
                    runId = runId.get()
                )
            ),
            buildOutput = project.buildOutput.get(),
            cdBuildConfig = project.cdBuildConfig.get(),
            versionCode = project.androidAppExtension.defaultConfig.versionCode.toString(),
            teamcityUrl = project.envArgs.build.url,
            gitState = gitState.get(),
        )
    }

    private fun createUploadAction(logger: Logger): UploadCdBuildResultTaskAction {
        return UploadCdBuildResultTaskAction(
            gson = uploadCdGson,
            client = Providers.client(
                user = user.get(),
                password = password.get(),
                logger = HttpLogger(logger)
            ),
            suppressErrors = suppressErrors.get()
        )
    }
}
