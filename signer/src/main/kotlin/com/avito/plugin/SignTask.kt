package com.avito.plugin

import com.avito.utils.BuildFailer
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class SignTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val tokenProperty = objects.property<String>()

    @Input
    val serviceUrl = objects.property<String>()

    @InputFile
    val unsignedFileProperty: RegularFileProperty = objects.fileProperty()

    @OutputFile
    val signedFileProperty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun run() {
        val unsignedFile = unsignedFileProperty.get().asFile
        val signedFile = signedFileProperty.get().asFile

        val serviceUrl: String = serviceUrl.get()

        val signResult = SignViaServiceAction(
            serviceUrl = serviceUrl,
            token = tokenProperty.get(),
            unsignedFile = unsignedFile,
            signedFile = signedFile,
            ciLogger = ciLogger
        ).sign() // TODO: User workers

        val buildFailer = BuildFailer.RealFailer()

        signResult.fold(
            { logger.info("signed successfully: ${signedFile.path}") },
            { buildFailer.failBuild("Can't sign: ${signedFile.path}; ${it.message}") }
        )
    }
}
