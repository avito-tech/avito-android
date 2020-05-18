package com.avito.plugin

import com.avito.utils.BuildFailer
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * TODO specify outputs: write a file after success upload (with link for example)
 * TODO check caching
 */
@Suppress("UnstableApiUsage")
abstract class QAppsUploadTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputFile
    abstract fun getApk(): RegularFileProperty // setup in ci build step

    @Input
    val comment = objects.property<String>()

    @Input
    val branch = objects.property<String>()

    @Input
    val host = objects.property<String>()

    @Input
    val versionName = objects.property<String>()

    @Input
    val versionCode = objects.property<String>()

    @Input
    val packageName = objects.property<String>()

    @Input
    val releaseChain: Property<Boolean> = objects.property<Boolean>().convention(false)

    @TaskAction
    fun upload() {
        val apk = getApk().asFile.get()

        val uploadResult = QAppsUploadAction(
            apk = apk,
            comment = comment.get(),
            host = host.get(),
            branch = branch.get(),
            versionName = versionName.get(),
            versionCode = versionCode.get(),
            packageName = packageName.get(),
            releaseChain = releaseChain.get(),
            logger = project.ciLogger
        ).upload()

        val buildFailer = BuildFailer.RealFailer()

        uploadResult.fold(
            { logger.info("Upload to qapps was successful: ${apk.path}") },
            { buildFailer.failBuild("Can't upload to qapps: ${apk.path};", it) }
        )
    }
}
