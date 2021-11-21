package com.avito.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class LegacySignBundleTask @Inject constructor(
    workerExecutor: WorkerExecutor
) : LegacySignArtifactTask(workerExecutor) {

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
        signedFile().copyTo(resultLocation(), overwrite = true)
    }

    /**
     * transform API is broken for ArtifactType.Bundle, hacking here to provide expected result
     * https://issuetracker.google.com/issues/174678813
     */
    private fun resultLocation(): File {
        return signedFile().path
            .replaceAfterLast("/", unsignedFile().name) // replace bugged `out` file name with original one
            .replace("/$name", "") // remove `signBundleViaServiceVariant` from path, bugged
            .let { File(it) }
    }
}
