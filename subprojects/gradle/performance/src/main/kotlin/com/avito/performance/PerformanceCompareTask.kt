package com.avito.performance

import com.avito.android.stats.statsdConfig
import com.avito.bitbucket.atlassianCredentials
import com.avito.bitbucket.bitbucketConfig
import com.avito.bitbucket.pullRequestId
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class PerformanceCompareTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Optional
    @InputFile
    val previousTests: RegularFileProperty = objects.fileProperty()

    //todo support @Optional
    @InputFile
    val currentTests: RegularFileProperty = objects.fileProperty()

    @Internal
    val reportApiUrl = objects.property<String>()

    @Internal
    val reportApiFallbackUrl = objects.property<String>()

    @Internal
    val statsUrl = objects.property<String>()

    @OutputFile
    val comparison: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {

        //todo новое api, когда выйдет в stable
        // https://docs.gradle.org/5.6/userguide/custom_tasks.html#using-the-worker-api
        @Suppress("DEPRECATION")
        workerExecutor.submit(PerformanceCompareAction::class.java) { workerConfiguration ->
            workerConfiguration.isolationMode = IsolationMode.NONE
            workerConfiguration.setParams(
                PerformanceCompareAction.Params(
                    previousTests = previousTests.orNull?.asFile,
                    currentTests = currentTests.asFile.get(),
                    comparison = comparison.asFile.get(),
                    buildUrl = project.envArgs.buildUrl,
                    logger = ciLogger,
                    reportApiUrl = reportApiUrl.get(),
                    reportApiFallbackUrl = reportApiFallbackUrl.get(),
                    atlassianCredentials = project.atlassianCredentials.get(),
                    pullRequestId = project.pullRequestId.orNull,
                    enablePrPerformanceReporting = project.getBooleanProperty("enablePrPerformanceReporting"),
                    bitbucketConfig = project.bitbucketConfig.get(),
                    statsdConfig = project.statsdConfig.get(),
                    //todo handle not defined
                    slackConfig = project.slackConfig.get(),
                    statsUrl = statsUrl.get()
                )
            )
        }
    }
}
