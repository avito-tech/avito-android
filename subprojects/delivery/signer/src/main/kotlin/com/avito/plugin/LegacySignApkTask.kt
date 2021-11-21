package com.avito.plugin

import com.avito.android.getApkOrThrow
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class LegacySignApkTask @Inject constructor(
    workerExecutor: WorkerExecutor
) : LegacySignArtifactTask(workerExecutor) {

    @get:InputDirectory
    public abstract val unsignedDirProperty: DirectoryProperty

    @get:OutputDirectory
    public abstract val signedDirProperty: DirectoryProperty

    override fun unsignedFile(): File {
        return unsignedDirProperty.get().getApkOrThrow()
    }

    override fun signedFile(): File {
        return File(signedDirProperty.get().asFile, signedFileName())
    }

    private fun signedFileName(): String {
        val unsignedFile = unsignedFile()

        val name = unsignedFile.nameWithoutExtension.removeSuffix("-unsigned")
        val extension = unsignedFile.extension

        return "$name.$extension"
    }

    override fun hackForArtifactsApi() {
        val signedFile = signedFile()
        val copyOfSigned = File(unsignedDirProperty.get().asFile, signedFileName())
        if (!copyOfSigned.exists()) {
            copyOfSigned.createNewFile()
        }
        signedFile.copyTo(copyOfSigned, overwrite = true)
    }
}
