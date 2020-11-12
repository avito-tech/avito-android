package com.avito.plugin

import com.avito.utils.BuildFailer
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class SignTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val tokenProperty = objects.property<String>()

    @Input
    val serviceUrl = objects.property<String>()

    @InputDirectory
    @Optional
    val unsignedDirProperty: DirectoryProperty = objects.directoryProperty()

    @OutputDirectory
    @Optional
    val signedDirProperty: DirectoryProperty = objects.directoryProperty()

    @InputFile
    @Optional
    val unsignedFileProperty: RegularFileProperty = objects.fileProperty()

    @OutputFile
    @Optional
    val signedFileProperty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun run() {
        val unsignedFile = getSingleFile(unsignedDirProperty, unsignedFileProperty)
        val signedFile = getSingleFile(signedDirProperty, signedFileProperty)

        val serviceUrl: String = serviceUrl.get()

        val signResult = SignViaServiceAction(
            serviceUrl = serviceUrl,
            token = tokenProperty.get(),
            unsignedFile = unsignedFile,
            signedFile = signedFile,
            ciLogger = ciLogger
        ).sign() // TODO: User workers

        hackForArtifactsApi(unsignedFile, signedFile)

        val buildFailer = BuildFailer.RealFailer()

        signResult.fold(
            { logger.info("signed successfully: ${signedFile.path}") },
            { buildFailer.failBuild("Can't sign: ${signedFile.path};", it) }
        )
    }

    private fun getSingleFile(dirProperty: DirectoryProperty, fileProperty: RegularFileProperty): File {
        require(dirProperty.isPresent xor fileProperty.isPresent)

        if (fileProperty.isPresent) {
            return fileProperty.asFile.get()
        }
        val files = dirProperty.files().files
        require(files.size == 1) {
            "Multiple files are not supported"
        }
        return files.first()
    }

    /**
     * TODO: find a better way to profile a final artifact to CI
     * Results of transformations are stored in build/intermediates/bundle/release/<task name>/out
     * It's accessible by Artifacts API programmatically but we need a final one file.
     * We rewrite an original file to preserve legacy behaviour.
     */
    private fun hackForArtifactsApi(inputFile: File, outputFile: File) {
        outputFile.copyTo(inputFile, overwrite = true)
    }
}
