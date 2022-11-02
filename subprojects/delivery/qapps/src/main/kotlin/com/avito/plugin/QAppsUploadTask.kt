package com.avito.plugin

import Slf4jGradleLoggerFactory
import com.avito.android.getApkOrThrow
import com.avito.utils.buildFailer
import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * TODO specify outputs: write a file after success upload (with link for example)
 * TODO test caching
 */
public abstract class QAppsUploadTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @Input
    public val variantName: Property<String> = objects.property()

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

    @Input
    public val releaseChain: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    public val releaseBuildVariants: ListProperty<String> = objects.listProperty()

    @get:InputDirectory
    public abstract val apkDirectory: DirectoryProperty

    @TaskAction
    public fun upload() {
        val apk = apkDirectory.get().getApkOrThrow()

        val uploadResult = QAppsUploadAction(
            apk = apk,
            comment = comment.get(),
            host = host.get(),
            branch = branch.get(),
            versionName = versionName.get(),
            versionCode = versionCode.get(),
            packageName = packageName.get(),
            releaseChain = releaseChain.getOrElse(false) && isReleaseBuildVariant(variantName.get()),
            loggerFactory = Slf4jGradleLoggerFactory,
            httpClientBuilder = OkHttpClient.Builder()
        ).upload()

        uploadResult.fold(
            { logger.info("Upload to qapps was successful: ${apk.path}") },
            { project.buildFailer.failBuild("Can't upload to qapps: ${apk.path};", it) }
        )
    }

    private fun isReleaseBuildVariant(variantName: String): Boolean {
        return releaseBuildVariants.map { variants ->
            variants.contains(variantName)
        }.getOrElse(false)
    }
}
