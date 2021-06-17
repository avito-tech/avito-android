package com.avito.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class SignBundleTask @Inject constructor(
    workerExecutor: WorkerExecutor
) : SignArtifactTask(workerExecutor) {

    @get:InputFile
    public abstract val unsignedFileProperty: RegularFileProperty

    @get:OutputFile
    public abstract val signedFileProperty: RegularFileProperty

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
