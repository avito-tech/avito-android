package com.avito.plugin

import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.BuildFailer
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class SignArtifactTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val tokenProperty = objects.property<String>()

    @Input
    val serviceUrl = objects.property<String>()

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
        val serviceUrl: String = serviceUrl.get()
        val httpClient = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
        ).provide()
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
        val signResult = SignViaServiceAction(
            serviceUrl = serviceUrl,
            httpClient = httpClient,
            token = tokenProperty.get(),
            unsignedFile = unsignedFile,
            signedFile = signedFile,
            loggerFactory = loggerFactory
        ).sign() // TODO: Use workers

        hackForArtifactsApi()

        val buildFailer = BuildFailer.RealFailer()

        val logger = loggerFactory.create<SignArtifactTask>()

        signResult.fold(
            { logger.info("signed successfully: ${signedFile.path}") },
            { buildFailer.failBuild("Can't sign: ${signedFile.path};", it) }
        )
    }
}

private const val TIMEOUT_SEC = 1L
