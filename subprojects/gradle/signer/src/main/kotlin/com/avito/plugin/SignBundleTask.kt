package com.avito.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class SignBundleTask @Inject constructor(objects: ObjectFactory) : SignArtifactTask(objects) {

    @InputFile
    val unsignedFileProperty: RegularFileProperty = objects.fileProperty()

    @OutputFile
    val signedFileProperty: RegularFileProperty = objects.fileProperty()

    override fun unsignedFile(): File {
        return unsignedFileProperty.get().asFile
    }

    override fun signedFile(): File {
        return signedFileProperty.get().asFile
    }

    override fun hackForArtifactsApi() {
        signedFile().copyTo(unsignedFile(), overwrite = true)
    }
}
