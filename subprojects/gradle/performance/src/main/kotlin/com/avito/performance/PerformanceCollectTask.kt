package com.avito.performance

import com.avito.android.stats.statsdConfig
import com.avito.bitbucket.bitbucketConfig
import com.avito.bitbucket.pullRequestId
import com.avito.report.model.ReportCoordinates
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class PerformanceCollectTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Input
    internal abstract val graphiteKey: Property<String>

    @get:Input
    internal abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Optional
    @get:Input
    abstract val buildId: Property<String>

    @get:Internal
    internal abstract val reportApiUrl: Property<String>

    @get:Internal
    internal abstract val reportApiFallbackUrl: Property<String>

    @get:OutputFile
    internal abstract val performanceTests: RegularFileProperty

    @TaskAction
    fun action() {

        //todo новое api, когда выйдет в stable
        // https://docs.gradle.org/5.6/userguide/custom_tasks.html#using-the-worker-api
        @Suppress("DEPRECATION")
        workerExecutor.submit(PerformanceCollectAction::class.java) { workerConfiguration ->
            workerConfiguration.isolationMode = IsolationMode.NONE
            workerConfiguration.setParams(
                PerformanceCollectAction.Params(
                    performanceTests = performanceTests.asFile.get(),
                    graphiteKey = graphiteKey.get(),
                    reportCoordinates = reportCoordinates.get(),
                    buildId = buildId.orNull,
                    buildUrl = project.envArgs.build.url,
                    logger = ciLogger,
                    reportApiUrl = reportApiUrl.get(),
                    reportApiFallbackUrl = reportApiFallbackUrl.get(),
                    pullRequestId = project.pullRequestId.orNull,
                    bitbucketConfig = project.bitbucketConfig.get(),
                    statsdConfig = project.statsdConfig.get(),
                    //todo handle not defined
                    slackConfig = project.slackConfig.get()
                )
            )
        }
    }
}
