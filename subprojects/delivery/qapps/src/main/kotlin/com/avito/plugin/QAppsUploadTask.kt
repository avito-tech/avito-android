package com.avito.plugin

import Slf4jGradleLoggerFactory
import com.avito.android.getApkOrThrow
import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.buildFailer
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * TODO specify outputs: write a file after success upload (with link for example)
 * TODO check caching
 */
public abstract class QAppsUploadTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    public val comment: Property<String> = objects.property()

    @Input
    public val branch: Property<String> = objects.property()

    @Input
    public val host: Property<String> = objects.property()

    @Input
    public val versionName: Property<String> = objects.property()

    @Input
    public val versionCode: Property<String> = objects.property()

    @Input
    public val packageName: Property<String> = objects.property()

    /**
     * modified by [com.avito.ci.steps.UploadToQapps] in release pipeline
     */
    @Input
    public val releaseChain: Property<Boolean> = objects.property<Boolean>().convention(false)

    @get:InputDirectory
    public abstract val apkDirectory: DirectoryProperty

    @TaskAction
    public fun upload() {
        val apk = apkDirectory.get().getApkOrThrow()

        val timeProvider: TimeProvider = DefaultTimeProvider()

        val httpClientProvider = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
            loggerFactory = Slf4jGradleLoggerFactory
        )

        val uploadResult = QAppsUploadAction(
            apk = apk,
            comment = comment.get(),
            host = host.get(),
            branch = branch.get(),
            versionName = versionName.get(),
            versionCode = versionCode.get(),
            packageName = packageName.get(),
            releaseChain = releaseChain.get(),
            httpClientProvider = httpClientProvider,
            loggerFactory = Slf4jGradleLoggerFactory
        ).upload()

        uploadResult.fold(
            { logger.info("Upload to qapps was successful: ${apk.path}") },
            { project.buildFailer.failBuild("Can't upload to qapps: ${apk.path};", it) }
        )
    }
}
