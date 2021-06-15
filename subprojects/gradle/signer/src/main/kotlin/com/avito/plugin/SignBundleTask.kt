package com.avito.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class SignBundleTask : SignArtifactTask() {

    @get:InputFile
    abstract val unsignedFileProperty: RegularFileProperty

    @get:OutputFile
    abstract val signedFileProperty: RegularFileProperty

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
