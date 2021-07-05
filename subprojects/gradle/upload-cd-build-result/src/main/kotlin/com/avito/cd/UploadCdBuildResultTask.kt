package com.avito.cd

import com.avito.android.androidAppExtension
import com.avito.git.gitState
import com.avito.http.HttpLogger
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.gradle.envArgs
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public abstract class UploadCdBuildResultTask : DefaultTask() {

    @get:Input
    public abstract val uiTestConfiguration: Property<String>

    @get:Input
    public abstract val user: Property<String>

    @get:Input
    public abstract val password: Property<String>

    @get:Input
    public abstract val suppressErrors: Property<Boolean>

    private val uploadAction by lazy {
        val logger = GradleLoggerFactory.getLogger(this)

        UploadCdBuildResultTaskAction(
            gson = uploadCdGson,
            client = Providers.client(
                user = user.get(),
                password = password.get(),
                logger = HttpLogger(logger)
            ),
            suppressErrors = suppressErrors.get()
        )
    }

    @TaskAction
    public fun sendCdBuildResult() {
        val gitState = project.gitState()
        uploadAction.send(
            buildOutput = project.buildOutput.get(),
            cdBuildConfig = project.cdBuildConfig.get(),
            versionCode = project.androidAppExtension.defaultConfig.versionCode.toString(),
            teamcityUrl = project.envArgs.build.url,
            gitState = gitState.get(),
            uiTestConfiguration = uiTestConfiguration.get()
        )
    }
}
