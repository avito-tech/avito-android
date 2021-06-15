package com.avito.plugin

import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.BuildFailer
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage", "LeakingThis")
abstract class SignArtifactTask : DefaultTask() {

    @get:Input
    abstract val tokenProperty: Property<String>

    @get:Input
    abstract val serviceUrl: Property<String>

    @get:Input
    abstract val readWriteTimeoutSec: Property<Long>

    protected abstract fun unsignedFile(): File

    protected abstract fun signedFile(): File

    /**
     * TODO: find a better way to provide a final artifact to CI
     * Results of transformations are stored in build/intermediates/bundle/release/<task name>/out
     * It's accessible by Artifacts API programmatically but we need a final one file.
     * We rewrite an original file to preserve legacy behaviour.
     */
    protected abstract fun hackForArtifactsApi()

    @TaskAction
    fun run() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val unsignedFile = unsignedFile()
        val signedFile = signedFile()
        val timeProvider: TimeProvider = DefaultTimeProvider()
        val serviceUrl: HttpUrl = serviceUrl.map { it.toHttpUrl() }.get()

        val timeout = readWriteTimeoutSec.get()

        val httpClient = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        ).provide()
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(
                RetryInterceptor(
                    retries = 3,
                    allowedMethods = listOf("GET", "POST")
                )
            )
            .build()

        // TODO: Use workers
        val signResult = SignViaServiceAction(
            serviceUrl = serviceUrl,
            httpClient = httpClient,
            token = tokenProperty.get(),
            unsignedFile = unsignedFile,
            signedFile = signedFile,
        ).sign()

        hackForArtifactsApi()

        val buildFailer = BuildFailer.RealFailer()

        val logger = loggerFactory.create<SignArtifactTask>()

        signResult.fold(
            { logger.info("signed successfully: ${signedFile.path}") },
            { buildFailer.failBuild("Can't sign: ${signedFile.path};", it) }
        )
    }
}
