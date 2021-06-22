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
        val agpVersion = project.providers.systemProperty("androidGradlePluginVersion").get()

        val to = when {
            agpVersion.startsWith("4.1") -> unsignedFile()
            agpVersion.startsWith("4.2") -> resultLocation()
            agpVersion.startsWith("7.") -> resultLocation()
            else -> throw IllegalArgumentException(
                "Unknown AGP version $agpVersion, can't say if Signer plugin will act correctly"
            )
        }

        signedFile().copyTo(to, overwrite = true)
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
