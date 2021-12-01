package com.avito.android.signer

import Slf4jGradleLoggerFactory
import com.avito.android.Problem
import com.avito.android.signer.internal.SignViaServiceAction
import com.avito.android.stats.statsd
import com.avito.gradle.worker.inMemoryWork
import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.buildFailer
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.util.concurrent.TimeUnit

public abstract class AbstractSignTask(
    private val workerExecutor: WorkerExecutor,
    objects: ObjectFactory
) : DefaultTask() {

    @get:Input
    public abstract val tokenProperty: Property<String>

    @get:Input
    public abstract val serviceUrl: Property<String>

    @get:Input
    public abstract val readWriteTimeoutSec: Property<Long>

    @get:OutputDirectory
    public val signedArtifactDirectory: Property<Directory> = objects.directoryProperty()

    protected abstract fun unsignedFile(): File

    protected abstract fun signedFilenameTransformer(unsignedFileName: String): String

    @TaskAction
    public fun doWork() {
        val unsignedFile = unsignedFile()
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

        val signedDirectory = signedArtifactDirectory.get().asFile.also {
            it.mkdirs()
        }

        val signedFile = File(signedDirectory, signedFilenameTransformer(unsignedFile.name))

        workerExecutor.inMemoryWork {
            SignViaServiceAction(
                serviceUrl = serviceUrl,
                httpClient = httpClient,
                token = tokenProperty.get(),
                unsignedFile = unsignedFile,
                signedFile = signedFile,
            )
                .sign()
                .fold(
                    { file -> logger.info("signed successfully: ${file.file.path}") },
                    { throwable ->
                        project.buildFailer.failBuild(
                            problem = describeSingingError(
                                signedFile = signedFile,
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
