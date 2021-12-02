package com.avito.plugin

import Slf4jGradleLoggerFactory
import com.avito.android.Problem
import com.avito.android.stats.statsd
import com.avito.gradle.worker.inMemoryWork
import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.plugin.internal.SignViaServiceAction
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.buildFailer
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.util.concurrent.TimeUnit

public abstract class LegacySignArtifactTask constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Input
    public abstract val tokenProperty: Property<String>

    @get:Input
    public abstract val serviceUrl: Property<String>

    @get:Input
    public abstract val readWriteTimeoutSec: Property<Long>

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
    public fun run() {
        val unsignedFile = unsignedFile()
        val signedFile = signedFile()
        val timeProvider: TimeProvider = DefaultTimeProvider()
        val serviceUrl: HttpUrl = serviceUrl.map { it.toHttpUrl() }.get()

        val timeout = readWriteTimeoutSec.get()

        val httpClient = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
            loggerFactory = Slf4jGradleLoggerFactory
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

        workerExecutor.inMemoryWork {
            SignViaServiceAction(
                serviceUrl = serviceUrl,
                httpClient = httpClient,
                token = tokenProperty.get(),
                unsignedFile = unsignedFile,
                signedFile = signedFile,
            )
                .sign()
                .map { file ->
                    hackForArtifactsApi()
                    file
                }
                .fold(
                    { file -> logger.info("signed successfully: ${file.file.path}") },
                    { throwable ->
                        project.buildFailer.failBuild(
                            problem = describeSingingError(
                                signedFile = signedFile(),
                                throwable = throwable
                            )
                        )
                    }
                )
        }
    }

    private fun describeSingingError(signedFile: File, throwable: Throwable): Problem {
        return Problem(
            shortDescription = "Can't sign: ${signedFile.path}",
            context = "Signing artifact via service",
            documentedAt = "https://avito-tech.github.io/avito-android/projects/internal/Signer/#troubleshooting",
            throwable = throwable
        )
    }
}
